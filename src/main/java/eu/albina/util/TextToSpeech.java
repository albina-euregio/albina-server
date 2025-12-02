// SPDX-License-Identifier: AGPL-3.0-or-later
package eu.albina.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
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

import com.google.common.base.Strings;
import com.google.common.collect.Streams;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.google.errorprone.annotations.CheckReturnValue;

import eu.albina.caaml.Caaml6;
import eu.albina.model.enumerations.DangerPattern;
import eu.albina.model.enumerations.LanguageCode;

@Singleton
public class TextToSpeech {
	private static final String API_URL = "https://eu-texttospeech.googleapis.com/v1/text:synthesize";
	static final String API_AUTH_SCOPE = "https://www.googleapis.com/auth/cloud-platform";
	private static final String jingle = "https://static.avalanche.report/synthesizer/intro_0_1.mp3";
	private static final Logger logger = LoggerFactory.getLogger(TextToSpeech.class);

	@Inject
	HttpClient httpClient;

	@Inject
	ObjectMapper objectMapper;

	enum SsmlVoiceGender {FEMALE, MALE}

	static class ScriptEngine {
		private final AvalancheBulletin bulletin;
		private final LanguageCode lang;
		private final SsmlVoiceGender gender;
		private final StringWriter stringWriter = new StringWriter();
		private final PrintWriter lines = new PrintWriter(stringWriter);

		ScriptEngine(AvalancheBulletin bulletin) {
			this.bulletin = bulletin;
			this.lang = LanguageCode.valueOf(bulletin.getLang());
			this.gender = bulletin.getBulletinID().chars().sum() % 2 == 1 ? SsmlVoiceGender.FEMALE : SsmlVoiceGender.MALE;
		}

		String createScript() {
			// https://cloud.google.com/text-to-speech/docs/ssml
			lines.format("<!--%s-->%n", voice());
			lines.println("<speak>");
			lines.println("<par>");
			lines.format("<media repeatCount=\"1\" fadeOutDur=\"10s\" end=\"10s\"><audio src=\"%s\"></audio></media>%n", jingle);
			lines.println("<media begin=\"+5s\">");

			String validityDate = getValidityDate(bulletin.getValidTime());
			paragraph(sentence(bulletin.getUnscheduled() ?
				lang.getBundleString("speech.bulletin.update", Map.of("validityDate", validityDate)) :
				lang.getBundleString("speech.bulletin", Map.of("validityDate", validityDate))));

			if (bulletin.getAvalancheActivity() != null) {
				paragraph(emphasis(sentence(bulletin.getAvalancheActivity().getHighlights())));
			}
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

			if (bulletin.getAvalancheActivity() != null) {
				paragraph(sentence(bulletin.getAvalancheActivity().getComment()));
			}
			break1s();

			paragraph(sentence(lang.getBundleString("speech.snowpack")));
			if (bulletin.getSnowpackStructure() != null) {
				paragraph(bulletin.getSnowpackStructure().getComment());
			}
			dangerPatterns();
			break1s();

			for (Tendency tendency : bulletin.getTendency()) {
				if (tendency == null || tendency.getTendencyType() == null) {
					continue;
				}
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
				.toList();
            return switch (texts.size()) {
                case 0 -> lang.getBundleString("speech.aspects.0");
                case 1 -> lang.getBundleString("speech.aspects.1", Map.of("aspect1", texts.get(0)));
                case 2 ->
                        lang.getBundleString("speech.aspects.2", Map.of("aspect1", texts.get(0), "aspect2", texts.get(1)));
                default ->
                        lang.getBundleString("speech.aspects.3", Map.of("aspect1", texts.get(0), "aspect2", texts.get(1), "aspect3", texts.get(2)));
            };
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
			return List.of(sorted.getFirst(), middleAspect, sorted.getLast());
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
				DangerRating rating = dangerRatings.getFirst();
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
			if (customData.getLWD_Tyrol() == null) {
				return;
			}
			String string = customData.getLWD_Tyrol().dangerPatterns.stream().map(String::valueOf)
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
			return lang.getLongDate(validTime.getStartTime().atZone(AlbinaUtil.localZone()).toLocalDate().plusDays(1));
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
				return new VoiceSelectionParams("de-DE", "de-DE-Wavenet-F", SsmlVoiceGender.FEMALE);
			} else if (lang == LanguageCode.de && gender == SsmlVoiceGender.MALE) {
				return new VoiceSelectionParams("de-DE", "de-DE-Wavenet-E", SsmlVoiceGender.MALE);
			} else if (lang == LanguageCode.en && gender == SsmlVoiceGender.FEMALE) {
				return new VoiceSelectionParams("en-GB", "en-GB-Wavenet-A", SsmlVoiceGender.FEMALE);
			} else if (lang == LanguageCode.en && gender == SsmlVoiceGender.MALE) {
				return new VoiceSelectionParams("en-GB", "en-GB-Wavenet-B", SsmlVoiceGender.MALE);
			} else if (lang == LanguageCode.it && gender == SsmlVoiceGender.MALE) {
				return new VoiceSelectionParams("it-IT", "it-IT-Wavenet-C", SsmlVoiceGender.MALE);
			} else if (lang == LanguageCode.it && gender == SsmlVoiceGender.FEMALE) {
				return new VoiceSelectionParams("it-IT", "it-IT-Wavenet-A", SsmlVoiceGender.FEMALE);
			} else if (lang == LanguageCode.es && gender == SsmlVoiceGender.MALE) {
				return new VoiceSelectionParams("es-ES", "es-ES-Wavenet-B", SsmlVoiceGender.MALE);
			} else if (lang == LanguageCode.es && gender == SsmlVoiceGender.FEMALE) {
				return new VoiceSelectionParams("es-ES", "es-ES-Wavenet-C", SsmlVoiceGender.FEMALE);
			} else if (lang == LanguageCode.ca) {
				return new VoiceSelectionParams("ca-ES", "ca-ES-Standard-A", SsmlVoiceGender.FEMALE);
			}
			throw new IllegalArgumentException();
		}

	}

	@Serdeable
	record VoiceSelectionParams(String languageCode, String name, SsmlVoiceGender ssmlGender) {
	}

	@Serdeable
	record Response(String audioContent) {
		byte[] asBytes() {
			return Base64.getDecoder().decode(audioContent);
		}
	}

	String createAudioFileRequest(AvalancheBulletin bulletin) {
		ScriptEngine scriptEngine = new ScriptEngine(bulletin);
		String ssml = scriptEngine.createScript();
		VoiceSelectionParams voice = scriptEngine.voice();

		logger.info("Synthesize speech for bulletin={} lang={} voice={}",
			bulletin.getBulletinID(), bulletin.getLang(), voice);

		// https://cloud.google.com/text-to-speech/docs/create-audio#text-to-speech-text-protocol
		try {
			return objectMapper.writeValueAsString(Map.of(
				"input", Map.of("ssml", ssml),
				"voice", voice,
				"audioConfig", Map.of("audioEncoding", "MP3")
			));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public byte[] createAudioFile(AvalancheBulletin bulletin) throws Exception {
		String json = createAudioFileRequest(bulletin);

		GoogleCredentials credentials = GoogleCredentials.ofEnv(objectMapper);
		GoogleCredentials.AccessToken accessToken = credentials.fetchAccessToken(httpClient, objectMapper);

		URI uri = URI.create(API_URL);
		HttpRequest request = HttpRequest.newBuilder(uri)
			.POST(HttpRequest.BodyPublishers.ofString(json))
			.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken.access_token())
			.header(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
			.timeout(Duration.ofSeconds(30))
			.build();
		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		Response audio = objectMapper.readValue(response.body(), Response.class);
		return audio.asBytes();
	}

	public void createAudioFiles(eu.albina.model.AvalancheReport avalancheReport) throws Exception {
		if (Strings.isNullOrEmpty(System.getenv("GOOGLE_APPLICATION_CREDENTIALS"))) {
			logger.info("Skipping synthesize speech since GOOGLE_APPLICATION_CREDENTIALS is undefined.");
			return;
		}
		for (eu.albina.model.AvalancheBulletin bulletin : avalancheReport.getBulletins()) {
			for (LanguageCode lang : avalancheReport.getRegion().getTTSLanguages()) {
				AvalancheBulletin caaml = Caaml6.toCAAML(bulletin, lang);
				String filename = String.format("%s_%s.ssml", caaml.getBulletinID(), lang);
				Path path = avalancheReport.getPdfDirectory().resolve(filename);
				if (!Files.exists(path)) {
					logger.info("Writing SSML file {}", path);
					Files.writeString(path, new ScriptEngine(caaml).createScript(), StandardCharsets.UTF_8);
				}
				filename = String.format("%s_%s.mp3", caaml.getBulletinID(), lang);
				path = avalancheReport.getPdfDirectory().resolve(filename);
				if (!Files.exists(path)) {
					byte[] audioFile = createAudioFile(caaml);
					logger.info("Writing audio file {} ({} bytes)", path, audioFile.length);
					Files.write(path, audioFile);
				}
			}
		}
	}

}
