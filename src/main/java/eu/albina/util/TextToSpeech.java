package eu.albina.util;

import com.google.cloud.texttospeech.v1.AudioConfig;
import com.google.cloud.texttospeech.v1.AudioEncoding;
import com.google.cloud.texttospeech.v1.SsmlVoiceGender;
import com.google.cloud.texttospeech.v1.SynthesisInput;
import com.google.cloud.texttospeech.v1.SynthesizeSpeechResponse;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.VoiceSelectionParams;
import com.google.common.collect.Streams;
import com.google.errorprone.annotations.CheckReturnValue;
import com.google.protobuf.ByteString;
import eu.albina.model.enumerations.DangerPattern;
import eu.albina.model.enumerations.LanguageCode;
import org.caaml.v6.AvalancheBulletin;
import org.caaml.v6.AvalancheProblem;
import org.caaml.v6.AvalancheProblemType;
import org.caaml.v6.DangerRating;
import org.caaml.v6.DangerRatingValue;
import org.caaml.v6.Tendency;
import org.caaml.v6.ValidTime;
import org.caaml.v6.ValidTimePeriod;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface TextToSpeech {
	String jingle = "https://storage.googleapis.com/avalnache-podcast-audio/intro_0_1.mp3";

	class ScriptEngine {
		private final AvalancheBulletin bulletin;
		private final LanguageCode lang;
		private final StringWriter stringWriter = new StringWriter();
		private final PrintWriter lines = new PrintWriter(stringWriter);

		private ScriptEngine(AvalancheBulletin bulletin) {
			this.bulletin = bulletin;
			this.lang = LanguageCode.valueOf(bulletin.getLang());
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

			paragraph(Streams.mapWithIndex(bulletin.getAvalancheProblems().stream().sorted(Comparator.comparingInt(p -> p.getProblemType().ordinal())), (avalancheProblem, i) -> {
				String avalancheProblemText = this.avalancheProblemText(avalancheProblem);
				if (i > 0) {
					avalancheProblemText = lang.getBundleString("speech.furthermore", Map.of("text", avalancheProblemText));
				}
				return avalancheProblemText;
			}).map(this::sentence));
			break1s();

			if (bulletin.getHighlights() != null) {
				paragraph(sentence(lang.getBundleString("speech.highlights", Map.of("highlights", bulletin.getHighlights()))));
			}

			paragraph(sentence(bulletin.getAvalancheActivity().getComment()));
			break1s();

			paragraph(sentence(lang.getBundleString("speech.snowpack")));
			paragraph(bulletin.getSnowpackStructure().getComment());
			if (bulletin.getCustomData() instanceof Map) {
				Object lwdTyrol = ((Map<?, ?>) bulletin.getCustomData()).get("LWD_Tyrol");
				if (lwdTyrol instanceof Map) {
					Object dangerPatterns = ((Map<?, ?>) lwdTyrol).get("dangerPatterns");
					if (dangerPatterns instanceof List) {
						String string = ((List<?>) dangerPatterns).stream().map(String::valueOf).map(DangerPattern::fromString).map(p -> p.toString(lang.getLocale())).collect(Collectors.joining(", "));
						lines.println(lang.getBundleString("speech.danger-patterns", Map.of("dangerPatterns", string)));
					}
				}
			}
			break1s();

			for (Tendency tendency : bulletin.getTendency()) {
				paragraph(sentence(this.tendencyText(tendency)));
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

		private String avalancheProblemText(AvalancheProblem p) {
			Map<String, String> options = Map.of(
				"avalancheProblemType", this.avalancheProblemTypeText(p.getProblemType()),
				"validTimePeriod", this.validTimePeriodText(p.getValidTimePeriod()),
				"lowerBound", p.getElevation() != null && p.getElevation().getLowerBound() != null
					? this.elevationText(p.getElevation().getLowerBound())
					: "",
				"upperBound", p.getElevation() != null && p.getElevation().getUpperBound() != null
					? this.elevationText(p.getElevation().getUpperBound())
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
			return lang.getBundle("i18n.AvalancheProblem").getString(problemType.toString());
		}

		private Stream<String> dangerRatingTexts(List<DangerRating> dangerRatings) {
			boolean isAllDay = dangerRatings.stream().allMatch(dr -> dr.getValidTimePeriod() == ValidTimePeriod.ALL_DAY);
			if (isAllDay) {
				return Stream.of(this.dangerRatingTexts0(dangerRatings, ValidTimePeriod.ALL_DAY));
			} else {
				return Stream.of(
					this.dangerRatingTexts0(dangerRatings, ValidTimePeriod.EARLIER),
					this.dangerRatingTexts0(dangerRatings, ValidTimePeriod.LATER));
			}
		}

		private String dangerRatingTexts0(List<DangerRating> dangerRatings, ValidTimePeriod validTimePeriod) {
			dangerRatings = dangerRatings.stream().filter(dr -> dr.getValidTimePeriod() == validTimePeriod).collect(Collectors.toList());
			if (dangerRatings.stream().allMatch(dr -> dr.getElevation() == null)) {
				DangerRating rating = dangerRatings.iterator().next();
				return lang.getBundleString("speech.danger-rating", Map.of(
					"validTimePeriod", this.validTimePeriodText(validTimePeriod),
					"dangerRating", this.dangerRatingValueText(rating.getMainValue())
				));
			} else {
				DangerRating upper = dangerRatings.stream().filter(dr -> dr.getElevation().getUpperBound() != null).findFirst().orElseThrow();
				DangerRating lower = dangerRatings.stream().filter(dr -> dr.getElevation().getLowerBound() != null).findFirst().orElseThrow();
				return lang.getBundleString("speech.danger-rating.above-below", Map.of(
					"validTimePeriod", this.validTimePeriodText(validTimePeriod),
					"elevationUpper", this.elevationText(lower.getElevation().getLowerBound()),
					"dangerRatingUpper", this.dangerRatingValueText(lower.getMainValue()),
					"elevationLower", this.elevationText(upper.getElevation().getUpperBound()),
					"dangerRatingLower", this.dangerRatingValueText(upper.getMainValue())
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
			return lang.getBundleString("valid-time-period." + validTimePeriod.toString());
		}

		private String tendencyText(Tendency tendency) {
			String tendencyDate = getValidityDate(tendency.getValidTime());
			return lang.getBundleString("speech.tendency." + tendency.getTendencyType(), Map.of("tendencyDate", tendencyDate));
		}

		private String getValidityDate(ValidTime validTime) {
			ZonedDateTime mainDate = eu.albina.model.AvalancheBulletin.getValidityDate(validTime.getStartTime().atZone(AlbinaUtil.localZone()));
			return AlbinaUtil.getDate(mainDate, lang);
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

	}

	static String createScript(AvalancheBulletin bulletin) {
		return new ScriptEngine(bulletin).createScript();
	}

	static void main(String... args) throws Exception {
		try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
			SynthesisInput input = SynthesisInput.newBuilder().setText("Hello, World!").build();
			VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
				.setLanguageCode("en-US")
				.setSsmlGender(SsmlVoiceGender.NEUTRAL)
				.build();
			AudioConfig audioConfig = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build();
			SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
			ByteString audioContents = response.getAudioContent();
			Path path = Path.of("output.mp3");
			Files.write(path, audioContents.toByteArray());
			System.out.println("Audio content written to file \"output.mp3\"");
		}
	}

}
