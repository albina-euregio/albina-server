package eu.albina.util;

import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.common.base.Strings;
import com.google.common.collect.Streams;
import com.google.errorprone.annotations.CheckReturnValue;
import com.google.protobuf.ByteString;
import eu.albina.caaml.Caaml6;
import eu.albina.model.enumerations.DangerPattern;
import eu.albina.model.enumerations.LanguageCode;
import org.caaml.v6.Aspect;
import org.caaml.v6.AvalancheBulletin;
import org.caaml.v6.AvalancheBulletinCustomData;
import org.caaml.v6.AvalancheProblem;
import org.caaml.v6.AvalancheProblemType;
import org.caaml.v6.DangerRating;
import org.caaml.v6.DangerRatingValue;
import org.caaml.v6.Tendency;
import org.caaml.v6.ValidTime;
import org.caaml.v6.ValidTimePeriod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface TextToSpeech {
	String jingle = "https://static.avalanche.report/synthesizer/intro_0_1.mp3";
	Set<LanguageCode> ENABLED = Collections.unmodifiableSet(EnumSet.of(LanguageCode.de, LanguageCode.en, LanguageCode.it, LanguageCode.es, LanguageCode.ca));
	Logger logger = LoggerFactory.getLogger(TextToSpeech.class);

	class ScriptEngine {
		private final AvalancheBulletin bulletin;
		private final LanguageCode lang;
		private final SsmlVoiceGender gender;
		private final StringWriter stringWriter = new StringWriter();
		private final PrintWriter lines = new PrintWriter(stringWriter);

		private ScriptEngine(AvalancheBulletin bulletin) {
			this.bulletin = bulletin;
			this.lang = LanguageCode.valueOf(bulletin.getLang());
			this.gender = bulletin.getBulletinID().chars().sum() % 2 == 1 ? SsmlVoiceGender.FEMALE : SsmlVoiceGender.MALE;
		}

		String createScript() {

			lines.println("<speak>");
			lines.println("<par>");
			lines.format("<media repeatCount=\"1\" fadeOutDur=\"10s\" end=\"10s\"><audio src=\"%s\"></audio></media>%n", jingle);
			lines.println("<media begin=\"+5s\">");

			String validityDate = getValidityDate(bulletin.getValidTime());
			paragraph(sentence(bulletin.getUnscheduled() ?
				lang.getBundleString("speech.bulletin.update", Map.of("validityDate", validityDate)) :
				lang.getBundleString("speech.bulletin", Map.of("validityDate", validityDate))));

			paragraph(emphasis(sentence(bulletin.getAvalancheActivity().getHighlights())));
			break1s();

			paragraph(dangerRatingTexts(bulletin.getDangerRatings()).map(this::sentence));
			break1s();

			paragraph(Streams.mapWithIndex(bulletin.getAvalancheProblems().stream().sorted(Comparator.comparingInt(p -> p.getValidTimePeriod().ordinal())), (avalancheProblem, i) -> {
				String avalancheProblemText = avalancheProblemText(avalancheProblem);
				if (i > 0) {
					avalancheProblemText = lang.getBundleString("speech.furthermore", Map.of("text", avalancheProblemText));
				}
				if (lang == LanguageCode.it) {
					avalancheProblemText = avalancheProblemText.replace("di limite del bosco", "del limite del bosco");
				}
				String aspectsText = aspectsText(Objects.requireNonNullElse(avalancheProblem.getAspects(), List.of()));
				return String.join(" ", avalancheProblemText, aspectsText);
			}).map(this::sentence));
			break1s();

			if (bulletin.getHighlights() != null) {
				paragraph(sentence(lang.getBundleString("speech.highlights", Map.of("highlights", bulletin.getHighlights()))));
			}

			paragraph(sentence(bulletin.getAvalancheActivity().getComment()));
			break1s();

			paragraph(sentence(lang.getBundleString("speech.snowpack")));
			paragraph(bulletin.getSnowpackStructure().getComment());
			dangerPatterns();
			break1s();

			for (Tendency tendency : bulletin.getTendency()) {
				paragraph(sentence(tendencyText(tendency)));
				break1s();
			}

			lines.println("</media>");
			lines.println("</par>");

			lines.println("<par>");
			lines.format("<media repeatCount=\"1\" fadeInDur=\"6s\" fadeOutDur=\"2s\"><audio src=\"%s\"></audio></media>", jingle);
			lines.println("<media>");
			paragraph(sentence(lang.getBundleString("speech.outro")));
			lines.println("</media>");
			lines.println("</par>");

			lines.println("</speak>");
			return stringWriter.toString().replace("<br/>", "<break time=\"1s\"/>");
		}

		private String aspectsText(List<Aspect> aspects) {
			List<String> texts = sortAspects(aspects).stream()
				.map(Aspect::toString)
				.map(lang.getBundle("i18n.Aspect")::getString)
				.collect(Collectors.toList());
			switch (texts.size()) {
				case 0:
					return lang.getBundleString("speech.aspects.0");
				case 1:
					return lang.getBundleString("speech.aspects.1", Map.of("aspect1", texts.get(0)));
				case 2:
					return lang.getBundleString("speech.aspects.2", Map.of("aspect1", texts.get(0), "aspect2", texts.get(1)));
				default:
					return lang.getBundleString("speech.aspects.3", Map.of("aspect1", texts.get(0), "aspect2", texts.get(1), "aspect3", texts.get(2)));
			}
		}


		private List<Aspect> sortAspects(List<Aspect> aspects) {
			List<Aspect> order = List.of(Aspect.N, Aspect.NE, Aspect.E, Aspect.SE, Aspect.S, Aspect.SW, Aspect.W, Aspect.NW);
			List<Aspect> sorted = Stream.concat(order.stream(), order.stream())
				.dropWhile(aspects::contains)
				.dropWhile(Predicate.not(aspects::contains))
				.takeWhile(aspects::contains)
				.collect(Collectors.toList());
			if (sorted.size() < 4) {
				return sorted;
			}
			Aspect middleAspect = Stream.of(Aspect.N, Aspect.S, Aspect.W, Aspect.E).filter(aspects::contains).findFirst().orElseThrow();
			return List.of(sorted.get(0), middleAspect, sorted.get(sorted.size() - 1));
		}

		private String avalancheProblemText(AvalancheProblem p) {
			Map<String, String> options = Map.of(
				"avalancheProblemType", avalancheProblemTypeText(p.getProblemType()),
				"validTimePeriod", validTimePeriodText(p.getValidTimePeriod()),
				"lowerBound", p.getElevation() != null && p.getElevation().getLowerBound() != null
					? elevationText(p.getElevation().getLowerBound())
					: "",
				"upperBound", p.getElevation() != null && p.getElevation().getUpperBound() != null
					? elevationText(p.getElevation().getUpperBound())
					: ""
			);
			if (!options.get("upperBound").isEmpty() && !options.get("lowerBound").isEmpty()) {
				return lang.getBundleString("speech.avalanche-problem.between", options);
			} else if (!options.get("upperBound").isEmpty()) {
				return lang.getBundleString("speech.avalanche-problem.below", options);
			} else if (!options.get("lowerBound").isEmpty()) {
				return lang.getBundleString("speech.avalanche-problem.above", options);
			} else {
				return lang.getBundleString("speech.avalanche-problem", options);
			}
		}

		private String avalancheProblemTypeText(AvalancheProblemType problemType) {
			return lang.getBundle("i18n.AvalancheProblem").getString(problemType.toString() + ".speech");
		}

		private Stream<String> dangerRatingTexts(List<DangerRating> dangerRatings) {
			boolean isAllDay = dangerRatings.stream().allMatch(dr -> dr.getValidTimePeriod() == ValidTimePeriod.ALL_DAY);
			if (isAllDay) {
				return Stream.of(dangerRatingTexts0(dangerRatings, ValidTimePeriod.ALL_DAY));
			} else {
				return Stream.of(
					dangerRatingTexts0(dangerRatings, ValidTimePeriod.EARLIER),
					dangerRatingTexts0(dangerRatings, ValidTimePeriod.LATER));
			}
		}

		private String dangerRatingTexts0(List<DangerRating> dangerRatings, ValidTimePeriod validTimePeriod) {
			dangerRatings = dangerRatings.stream().filter(dr -> dr.getValidTimePeriod() == validTimePeriod).collect(Collectors.toList());
			if (dangerRatings.stream().allMatch(dr -> dr.getElevation() == null)) {
				DangerRating rating = dangerRatings.iterator().next();
				return lang.getBundleString("speech.danger-rating", Map.of(
					"validTimePeriod", validTimePeriodText(validTimePeriod),
					"dangerRating", dangerRatingValueText(rating.getMainValue())
				));
			} else {
				DangerRating upper = dangerRatings.stream().filter(dr -> dr.getElevation().getUpperBound() != null).findFirst().orElseThrow();
				DangerRating lower = dangerRatings.stream().filter(dr -> dr.getElevation().getLowerBound() != null).findFirst().orElseThrow();
				return lang.getBundleString("speech.danger-rating.above-below", Map.of(
					"validTimePeriod", validTimePeriodText(validTimePeriod),
					"elevationUpper", elevationText(lower.getElevation().getLowerBound()),
					"dangerRatingUpper", dangerRatingValueText(lower.getMainValue()),
					"elevationLower", elevationText(upper.getElevation().getUpperBound()),
					"dangerRatingLower", dangerRatingValueText(upper.getMainValue())
				));
			}
		}

		private String dangerRatingValueText(DangerRatingValue mainValue) {
			return lang.getBundle("i18n.DangerRating").getString(mainValue.toString() + ".speech");
		}

		private String elevationText(String elevation) {
			return "treeline".equalsIgnoreCase(elevation)
				? lang.getBundleString("speech.elevation.treeline")
				: lang.getBundleString("speech.elevation.meter", Map.of("elevation", elevation));
		}

		private String validTimePeriodText(ValidTimePeriod validTimePeriod) {
			return lang.getBundleString("valid-time-period." + validTimePeriod.toString() + ".long");
		}

		private void dangerPatterns() {
			if (!(bulletin.getCustomData() instanceof AvalancheBulletinCustomData)) {
				return;
			}
			AvalancheBulletinCustomData customData = (AvalancheBulletinCustomData) bulletin.getCustomData();
			if (customData.LWD_Tyrol == null) {
				return;
			}
			String string = customData.LWD_Tyrol.dangerPatterns.stream().map(String::valueOf)
				.map(DangerPattern::fromString)
				.map(p -> p.toString(lang.getLocale()))
				.collect(Collectors.joining(", "));
			if (string.isEmpty()) {
				return;
			}
			lines.println(lang.getBundleString("speech.danger-patterns", Map.of("dangerPatterns", string)));
		}

		private String tendencyText(Tendency tendency) {
			String tendencyDate = getValidityDate(tendency.getValidTime());
			return lang.getBundleString("speech.tendency." + tendency.getTendencyType(), Map.of("tendencyDate", tendencyDate));
		}

		private String getValidityDate(ValidTime validTime) {
			return lang.getLongDate(validTime.getStartTime().atZone(AlbinaUtil.localZone()));
		}

		void paragraph(String element) {
			lines.printf("<p>%s</p>%n", element);
		}

		void paragraph(Stream<String> elements) {
			lines.println("<p>");
			elements.forEach(lines::println);
			lines.println("</p>");
		}

		@CheckReturnValue
		String sentence(String text) {
			return String.format("<s>%s</s>", text);
		}

		@CheckReturnValue
		String emphasis(String text) {
			return String.format("<emphasis>%s</emphasis>", text);
		}

		void break1s() {
			lines.println("<break time=\"1s\" strength=\"strong\"></break>");
		}

		VoiceSelectionParams voice() {
			// https://cloud.google.com/text-to-speech/docs/voices
			if (lang == LanguageCode.de && gender == SsmlVoiceGender.FEMALE) {
				return VoiceSelectionParams.newBuilder()
					.setLanguageCode("de-DE")
					.setName("de-DE-Wavenet-F")
					.setSsmlGender(SsmlVoiceGender.FEMALE)
					.build();
			} else if (lang == LanguageCode.de && gender == SsmlVoiceGender.MALE) {
				return VoiceSelectionParams.newBuilder()
					.setLanguageCode("de-DE")
					.setName("de-DE-Wavenet-E")
					.setSsmlGender(SsmlVoiceGender.MALE)
					.build();
			} else if (lang == LanguageCode.en && gender == SsmlVoiceGender.FEMALE) {
				return VoiceSelectionParams.newBuilder()
					.setLanguageCode("en-GB")
					.setName("en-GB-Wavenet-A")
					.setSsmlGender(SsmlVoiceGender.FEMALE)
					.build();
			} else if (lang == LanguageCode.en && gender == SsmlVoiceGender.MALE) {
				return VoiceSelectionParams.newBuilder()
					.setLanguageCode("en-GB")
					.setName("en-GB-Wavenet-B")
					.setSsmlGender(SsmlVoiceGender.MALE)
					.build();
			} else if (lang == LanguageCode.it && gender == SsmlVoiceGender.MALE) {
				return VoiceSelectionParams.newBuilder()
					.setLanguageCode("it-IT")
					.setName("it-IT-Wavenet-C")
					.setSsmlGender(SsmlVoiceGender.MALE)
					.build();
			} else if (lang == LanguageCode.it && gender == SsmlVoiceGender.FEMALE) {
				return VoiceSelectionParams.newBuilder()
					.setLanguageCode("it-IT")
					.setName("it-IT-Wavenet-A")
					.setSsmlGender(SsmlVoiceGender.FEMALE)
					.build();
			} else if (lang == LanguageCode.es && gender == SsmlVoiceGender.MALE) {
				return VoiceSelectionParams.newBuilder()
					.setLanguageCode("es-ES")
					.setName("es-ES-Wavenet-B")
					.setSsmlGender(SsmlVoiceGender.MALE)
					.build();
			} else if (lang == LanguageCode.es && gender == SsmlVoiceGender.FEMALE) {
				return VoiceSelectionParams.newBuilder()
					.setLanguageCode("es-ES")
					.setName("es-ES-Wavenet-C")
					.setSsmlGender(SsmlVoiceGender.FEMALE)
					.build();
			} else if (lang == LanguageCode.ca) {
				return VoiceSelectionParams.newBuilder()
					.setLanguageCode("ca-ES")
					.setName("ca-ES-Standard-A")
					.setSsmlGender(SsmlVoiceGender.FEMALE)
					.build();
			}
			throw new IllegalArgumentException();
		}

		AudioConfig audioConfig() {
			return AudioConfig.newBuilder()
				.setAudioEncoding(AudioEncoding.MP3)
				.addEffectsProfileId("handset-class-device")
				.build();
		}

	}

	static String createScript(AvalancheBulletin bulletin) {
		ScriptEngine scriptEngine = new ScriptEngine(bulletin);
		String ssml = scriptEngine.createScript();
		VoiceSelectionParams voice = scriptEngine.voice();
		AudioConfig audioConfig = scriptEngine.audioConfig();
		return String.format("<!--%n%s%s-->%n%s", voice, audioConfig, ssml);
	}

	static ByteString createAudioFile(AvalancheBulletin bulletin) throws Exception {
		ScriptEngine scriptEngine = new ScriptEngine(bulletin);
		String ssml = scriptEngine.createScript();
		SynthesisInput input = SynthesisInput.newBuilder().setSsml(ssml).build();
		VoiceSelectionParams voice = scriptEngine.voice();
		AudioConfig audioConfig = scriptEngine.audioConfig();
		try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
			logger.info("Synthesize speech for bulletin={} lang={} voice={} audioConfig={}",
				bulletin.getBulletinID(), bulletin.getLang(), voice, audioConfig);
			SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
			return response.getAudioContent();
		}
	}

	static void createAudioFiles(eu.albina.model.AvalancheReport avalancheReport) throws Exception {
		if (Strings.isNullOrEmpty(System.getenv("GOOGLE_APPLICATION_CREDENTIALS"))) {
			logger.info("Skipping synthesize speech since GOOGLE_APPLICATION_CREDENTIALS is undefined.");
			return;
		}
		for (eu.albina.model.AvalancheBulletin bulletin : avalancheReport.getBulletins()) {
			for (LanguageCode lang : ENABLED) {
				AvalancheBulletin caaml = Caaml6.toCAAML(bulletin, lang);
				ByteString audioFile = createAudioFile(caaml);
				String filename = String.format("%s_%s_%s.mp3", avalancheReport.getRegion().getId(), caaml.getBulletinID(), lang.toString());
				Path path = avalancheReport.getPdfDirectory().resolve(filename);
				logger.info("Writing audio file {} ({} bytes)", path, audioFile.size());
				Files.write(path, audioFile.toByteArray());
				AlbinaUtil.setFilePermissions(path.toString());
			}
		}
	}

}
