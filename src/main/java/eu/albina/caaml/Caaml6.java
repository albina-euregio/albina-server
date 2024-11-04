package eu.albina.caaml;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.AvalancheProblem;
import eu.albina.model.AvalancheReport;
import eu.albina.model.EawsMatrixInformation;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;
import org.caaml.v6.AvalancheBulletinCustomData;
import org.caaml.v6.AvalancheBulletins;
import org.caaml.v6.AvalancheProblemCustomData;
import org.caaml.v6.AvalancheProblemType;
import org.caaml.v6.Tendency;
import org.caaml.v6.DangerRatingValue;
import org.caaml.v6.ElevationBoundaryOrBand;
import org.caaml.v6.ExpectedAvalancheFrequency;
import org.caaml.v6.ExpectedSnowpackStability;
import org.caaml.v6.TendencyType;
import org.caaml.v6.ValidTime;
import org.caaml.v6.ValidTimePeriod;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Caaml6 {
	static String createJSON(AvalancheReport avalancheReport, LanguageCode lang) {
		try {
			return new ObjectMapper()
				.setSerializationInclusion(JsonInclude.Include.NON_NULL)
				.writerWithDefaultPrettyPrinter()
				.writeValueAsString(toCAAML(avalancheReport, lang));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	static String createXML(AvalancheReport avalancheReport, LanguageCode lang) {
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

	static org.caaml.v6.AvalancheBulletins toCAAML(AvalancheReport avalancheReport, LanguageCode lang) {
		return new AvalancheBulletins(avalancheReport.getBulletins().stream().map(b -> toCAAML(b, lang)).collect(Collectors.toList()));
	}

	static org.caaml.v6.AvalancheBulletin toCAAML(AvalancheBulletin avalancheBulletin, LanguageCode lang) {
		org.caaml.v6.AvalancheBulletin bulletin = new org.caaml.v6.AvalancheBulletin();
		bulletin.setUnscheduled(AvalancheReport.of(List.of(avalancheBulletin), null, null).isUpdate());
		bulletin.setAvalancheActivity(new org.caaml.v6.Texts(avalancheBulletin.getAvActivityHighlightsIn(lang), avalancheBulletin.getAvActivityCommentIn(lang)));
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
				getDangerRating(avalancheBulletin, daytime, daytime.getDangerRatingBelow()),
				getDangerRating(avalancheBulletin, daytime, daytime.getDangerRatingAbove())))
			.distinct().collect(Collectors.toList()));
		bulletin.setHighlights(avalancheBulletin.getHighlightsIn(lang));
		bulletin.setLang(lang.name());
		bulletin.setMetaData(null);
		bulletin.setPublicationTime(avalancheBulletin.getPublicationDate().toInstant().truncatedTo(ChronoUnit.SECONDS));
		bulletin.setRegions(avalancheBulletin.getPublishedRegions().stream()
			.map(id -> new org.caaml.v6.Region(id, lang.getRegionName(id)))
			.collect(Collectors.toList()));
		bulletin.setSnowpackStructure(new org.caaml.v6.Texts(avalancheBulletin.getSnowpackStructureHighlightsIn(lang), avalancheBulletin.getSnowpackStructureCommentIn(lang)));
		bulletin.setSource(null);
		bulletin.setTendency(List.of(new Tendency(
			avalancheBulletin.getTendencyCommentIn(lang),
			avalancheBulletin.getTendency() != null ? TendencyType.forValue(avalancheBulletin.getTendency().name()) : null,
			new ValidTime(avalancheBulletin.getValidFrom().plusDays(1).toInstant(), avalancheBulletin.getValidUntil().plusDays(1).toInstant())
		)));
		bulletin.setTravelAdvisory(new org.caaml.v6.Texts(avalancheBulletin.getTravelAdvisoryHighlightsIn(lang), avalancheBulletin.getTravelAdvisoryCommentIn(lang)));
		bulletin.setValidTime(new ValidTime(avalancheBulletin.getValidFrom().toInstant(), avalancheBulletin.getValidUntil().toInstant()));
		bulletin.setWeatherForecast(new org.caaml.v6.Texts(avalancheBulletin.getSynopsisHighlightsIn(lang), avalancheBulletin.getSynopsisCommentIn(lang)));
		return bulletin;
	}

	static org.caaml.v6.AvalancheProblem getAvalancheProblem(AvalancheProblem p, LanguageCode lang, ValidTimePeriod validTimePeriod) {
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

	static org.caaml.v6.DangerRating getDangerRating(AvalancheBulletin avalancheBulletin, AvalancheBulletinDaytimeDescription daytime, DangerRating rating) {
		org.caaml.v6.DangerRating result = new org.caaml.v6.DangerRating();
		if (!daytime.isHasElevationDependency() || Objects.equals(daytime.getDangerRatingBelow(), daytime.getDangerRatingAbove())) {
			result.setMainValue(DangerRatingValue.forValue(daytime.getDangerRatingAbove().name()));
			result.setElevation(null);
		} else if (rating == daytime.getDangerRatingAbove()) {
			result.setMainValue(DangerRatingValue.forValue(rating.name()));
			String bound = daytime.getTreeline() ? "treeline" : Integer.toString(daytime.getElevation());
			result.setElevation(new ElevationBoundaryOrBand(bound, null));
		} else if (rating == daytime.getDangerRatingBelow()) {
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
