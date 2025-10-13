package eu.albina.caaml;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.base.Strings;
import io.micronaut.serde.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.caaml.v6.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.AvalancheProblem;
import eu.albina.model.AvalancheReport;
import eu.albina.model.EawsMatrixInformation;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;

@Singleton
public class Caaml6 {

	@Inject
	private ObjectMapper objectMapper;

	public String createJSON(AvalancheReport avalancheReport, LanguageCode lang) {
		try {
			return objectMapper
				.writeValueAsString(toCAAML(avalancheReport, lang));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String createXML(AvalancheReport avalancheReport, LanguageCode lang) {
		try {
			return new XmlMapper()
				.setSerializationInclusion(JsonInclude.Include.NON_NULL)
				.writerWithDefaultPrettyPrinter()
				.writeValueAsString(toCAAML(avalancheReport, lang))
				.replaceFirst("^<bulletins>", String.format("<bulletins xmlns=\"%s\">", CaamlVersion.V6.namespace()));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static org.caaml.v6.AvalancheBulletins toCAAML(AvalancheReport avalancheReport, LanguageCode lang) {
		AvalancheBulletins bulletins = new AvalancheBulletins(avalancheReport.getBulletins().stream().map(b -> toCAAML(b, lang)).collect(Collectors.toList()));
		// TODO (general-headline): insert conditional - only set general headline if activated in backend
		String generalHeadline = avalancheReport.getGeneralHeadline(lang);
		if (!Strings.isNullOrEmpty(generalHeadline)) {
			bulletins.setCustomData(new AvalancheBulletinsCustomData(
				new AvalancheBulletinsCustomData.ALBINA(generalHeadline)
			));
		}
		return bulletins;
	}

	public static org.caaml.v6.AvalancheBulletin toCAAML(AvalancheBulletin avalancheBulletin, LanguageCode lang) {
		org.caaml.v6.AvalancheBulletin bulletin = new org.caaml.v6.AvalancheBulletin();
		bulletin.setUnscheduled(avalancheBulletin.isUpdate());
		bulletin.setAvalancheActivity(org.caaml.v6.Texts.of(avalancheBulletin.getAvActivityHighlightsIn(lang), avalancheBulletin.getAvActivityCommentIn(lang)));
		if (avalancheBulletin.isHasDaytimeDependency()) {
			bulletin.setAvalancheProblems(Stream.concat(
				avalancheBulletin.getForenoon().getAvalancheProblems().stream().map(p -> getAvalancheProblem(p, lang, ValidTimePeriod.EARLIER)),
				avalancheBulletin.getAfternoon().getAvalancheProblems().stream().map(p -> getAvalancheProblem(p, lang, ValidTimePeriod.LATER))
			).filter(Objects::nonNull).collect(Collectors.toList()));
		} else {
			bulletin.setAvalancheProblems(avalancheBulletin.getForenoon().getAvalancheProblems().stream()
				.map(p -> getAvalancheProblem(p, lang, ValidTimePeriod.ALL_DAY))
				.filter(Objects::nonNull).collect(Collectors.toList()));
		}
		bulletin.setBulletinID(avalancheBulletin.getId());
		List<String> dangerPatterns = Stream.of(avalancheBulletin.getDangerPattern1(), avalancheBulletin.getDangerPattern2())
			.filter(Objects::nonNull)
			.map(dp -> dp.name().toUpperCase())
			.collect(Collectors.toList());
		bulletin.setCustomData(new AvalancheBulletinCustomData(
			new AvalancheBulletinCustomData.ALBINA(avalancheBulletin.getValidityDateString()),
			new AvalancheBulletinCustomData.LwdTyrol(dangerPatterns)
		));
		bulletin.setDangerRatings(Stream.of(avalancheBulletin.getForenoon(), avalancheBulletin.getAfternoon())
			.filter(Objects::nonNull)
			.flatMap(daytime -> Stream.of(
				getDangerRating(avalancheBulletin, daytime, daytime.dangerRating(false)),
				getDangerRating(avalancheBulletin, daytime, daytime.dangerRating(true))))
			.distinct().collect(Collectors.toList()));
		bulletin.setHighlights(avalancheBulletin.getHighlightsIn(lang));
		bulletin.setLang(lang.name());
		bulletin.setMetaData(null);
		bulletin.setPublicationTime(avalancheBulletin.getPublicationDate().toInstant().truncatedTo(ChronoUnit.SECONDS));
		bulletin.setRegions(avalancheBulletin.getPublishedRegions().stream()
			.map(id -> new org.caaml.v6.Region(id, lang.getRegionName(id)))
			.collect(Collectors.toList()));
		bulletin.setSnowpackStructure(org.caaml.v6.Texts.of(avalancheBulletin.getSnowpackStructureHighlightsIn(lang), avalancheBulletin.getSnowpackStructureCommentIn(lang)));
		bulletin.setSource(null);
		bulletin.setTendency(List.of(new Tendency(
			avalancheBulletin.getTendencyCommentIn(lang),
			avalancheBulletin.getTendency() != null ? TendencyType.forValue(avalancheBulletin.getTendency().name()) : null,
			new ValidTime(avalancheBulletin.getValidFrom().plusDays(1).toInstant(), avalancheBulletin.getValidUntil().plusDays(1).toInstant())
		)));
		bulletin.setTravelAdvisory(org.caaml.v6.Texts.of(avalancheBulletin.getTravelAdvisoryHighlightsIn(lang), avalancheBulletin.getTravelAdvisoryCommentIn(lang)));
		bulletin.setValidTime(new ValidTime(avalancheBulletin.getValidFrom().toInstant(), avalancheBulletin.getValidUntil().toInstant()));
		bulletin.setWeatherForecast(org.caaml.v6.Texts.of(avalancheBulletin.getSynopsisHighlightsIn(lang), avalancheBulletin.getSynopsisCommentIn(lang)));
		return bulletin;
	}

	public static org.caaml.v6.AvalancheProblem getAvalancheProblem(AvalancheProblem p, LanguageCode lang, ValidTimePeriod validTimePeriod) {
		if (p == null || p.getAvalancheProblem() == null) {
			return null;
		}
		final org.caaml.v6.AvalancheProblem result = new org.caaml.v6.AvalancheProblem();
		result.setAspects(p.getAspects().stream().map(a -> org.caaml.v6.Aspect.forValue(a.name())).collect(Collectors.toList()));
		result.setProblemType(AvalancheProblemType.forValue(p.getAvalancheProblem().toString()));
		final String lowerBound = p.getElevationLow() > 0 ? Integer.toString(p.getElevationLow()) : p.getTreelineLow() ? "treeline" : null;
		final String upperBound = p.getElevationHigh() > 0 ? Integer.toString(p.getElevationHigh()) : p.getTreelineHigh() ? "treeline" : null;
		result.setElevation(new ElevationBoundaryOrBand(lowerBound, upperBound));
		result.setComment(p.getTerrainFeature(lang));
		result.setValidTimePeriod(validTimePeriod);
		final EawsMatrixInformation matrixInformation = p.getEawsMatrixInformation();
		if (matrixInformation != null) {
			if (matrixInformation.getAvalancheSize() != null) {
				result.setAvalancheSize(matrixInformation.getAvalancheSize().toInteger());
			}
			result.setFrequency(ExpectedAvalancheFrequency.forValue(matrixInformation.getFrequency().toString()));
			result.setSnowpackStability(ExpectedSnowpackStability.forValue(matrixInformation.getSnowpackStability().toString()));
		}
		if (p.getAvalancheType() != null) {
			result.setCustomData(new AvalancheProblemCustomData(
				new AvalancheProblemCustomData.ALBINA(p.getAvalancheType().toString())
			));
		}

		return result;
	}

	public static org.caaml.v6.DangerRating getDangerRating(AvalancheBulletin avalancheBulletin, AvalancheBulletinDaytimeDescription daytime, DangerRating rating) {
		org.caaml.v6.DangerRating result = new org.caaml.v6.DangerRating();
		if (!daytime.isHasElevationDependency() || Objects.equals(daytime.dangerRating(false), daytime.dangerRating(true))) {
			result.setMainValue(DangerRatingValue.forValue(daytime.dangerRating(true).name()));
			result.setElevation(null);
		} else if (rating == daytime.dangerRating(true)) {
			result.setMainValue(DangerRatingValue.forValue(rating.name()));
			String bound = daytime.getTreeline() ? "treeline" : Integer.toString(daytime.getElevation());
			result.setElevation(new ElevationBoundaryOrBand(bound, null));
		} else if (rating == daytime.dangerRating(false)) {
			result.setMainValue(DangerRatingValue.forValue(rating.name()));
			String bound = daytime.getTreeline() ? "treeline" : Integer.toString(daytime.getElevation());
			result.setElevation(new ElevationBoundaryOrBand(null, bound));
		}
		if (!avalancheBulletin.isHasDaytimeDependency()) {
			result.setValidTimePeriod(ValidTimePeriod.ALL_DAY);
		} else if (daytime == avalancheBulletin.getForenoon()) {
			result.setValidTimePeriod(ValidTimePeriod.EARLIER);
		} else if (daytime == avalancheBulletin.getAfternoon()) {
			result.setValidTimePeriod(ValidTimePeriod.LATER);
		} else {
			result.setValidTimePeriod(null);
		}
		return result;
	}
}
