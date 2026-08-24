package eu.albina.caaml;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import com.google.common.base.Strings;
import io.micronaut.serde.ObjectMapper;
import org.caaml.v6.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.core.JacksonException;
import tools.jackson.dataformat.xml.XmlMapper;

import eu.albina.model.AvalancheBulletin;
import eu.albina.model.AvalancheBulletinDaytimeDescription;
import eu.albina.model.AvalancheBulletinPhoto;
import eu.albina.model.AvalancheProblem;
import eu.albina.model.AvalancheReport;
import eu.albina.model.EawsMatrixInformation;
import eu.albina.model.enumerations.DangerRating;
import eu.albina.model.enumerations.LanguageCode;

public record Caaml6(AvalancheReport avalancheReport, List<AvalancheReport> previousReports, LanguageCode lang) {

	public String createJSON(ObjectMapper objectMapper) {
		try {
			return objectMapper
				.writeValueAsString(toCAAML());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String createXML() {
		try {
			// Jackson 3: mappers are immutable and configured via the builder;
			// java.time support is built in (no JavaTimeModule registration needed).
			return XmlMapper.builder()
				// Jackson 3 sorts properties alphabetically by default; keep declaration order (Jackson 2 behaviour).
				.disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
				.disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
				.changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
				.build()
				.writerWithDefaultPrettyPrinter()
				.writeValueAsString(toCAAML())
				.replaceFirst("^<bulletins>", String.format("<bulletins xmlns=\"%s\">", CaamlVersion.V6.namespace()));
		} catch (JacksonException e) {
			throw new RuntimeException(e);
		}
	}

	public org.caaml.v6.AvalancheBulletins toCAAML() {
		AvalancheBulletins bulletins = new AvalancheBulletins(avalancheReport.getBulletins().stream().map(b -> toCAAML(b, lang)).toList());
		avalancheReport.getGeneralHeadline(lang).ifPresent(generalHeadline -> {
			bulletins.setCustomData(new AvalancheBulletinsCustomData(
				new AvalancheBulletinsCustomData.ALBINA(generalHeadline)
			));
		});
		return bulletins;
	}

	org.caaml.v6.AvalancheBulletin toCAAML(AvalancheBulletin avalancheBulletin, LanguageCode lang) {
		org.caaml.v6.AvalancheBulletin bulletin = new org.caaml.v6.AvalancheBulletin();
		bulletin.setUnscheduled(avalancheBulletin.isUpdate());
		bulletin.setAvalancheActivity(org.caaml.v6.Texts.of(avalancheBulletin.getAvActivityHighlightsIn(lang), avalancheBulletin.getAvActivityCommentIn(lang)));
		if (avalancheBulletin.isHasDaytimeDependency()) {
			bulletin.setAvalancheProblems(Stream.concat(
				avalancheBulletin.getForenoon().getAvalancheProblems().stream().map(p -> getAvalancheProblem(p, ValidTimePeriod.earlier)),
				avalancheBulletin.getAfternoon().getAvalancheProblems().stream().map(p -> getAvalancheProblem(p, ValidTimePeriod.later))
			).filter(Objects::nonNull).toList());
		} else {
			bulletin.setAvalancheProblems(avalancheBulletin.getForenoon().getAvalancheProblems().stream()
				.map(p -> getAvalancheProblem(p, ValidTimePeriod.all_day))
				.filter(Objects::nonNull).toList());
		}
		bulletin.setBulletinID(avalancheBulletin.getId());
		List<String> dangerPatterns = Stream.of(avalancheBulletin.getDangerPattern1(), avalancheBulletin.getDangerPattern2())
			.filter(Objects::nonNull)
			.map(dp -> dp.name().toUpperCase())
			.toList();
		List<AvalancheBulletinCustomData.BulletinPhoto> images = avalancheBulletin.getPhotos() == null
			? List.of()
			: avalancheBulletin.getPhotos().stream()
				.map(Caaml6::toCaamlBulletinPhoto)
				.toList();
		AvalancheBulletinCustomData.TendencyProgression tendencyProgression = new AvalancheBulletinCustomData.TendencyProgression(
			previousReports.stream().map(AvalancheReport::getValidityDate).toList(),
			avalancheBulletin.getPublishedRegions().stream().collect(Collectors.toMap(
				r -> r,
				region -> previousReports.stream().map(r -> r.getBulletins().stream()
					.filter(b -> b.getPublishedRegions().contains(region))
					.findFirst()
					.stream()
					.flatMap(Caaml6::getDangerRatings)
					.map(org.caaml.v6.DangerRating::getMainValue)
					.max(Comparator.naturalOrder())
					.orElse(null)
				).toList()
			))
		);
		bulletin.setCustomData(new AvalancheBulletinCustomData(
			new AvalancheBulletinCustomData.ALBINA(
				avalancheBulletin.getValidityDateString(),
				tendencyProgression.dates().isEmpty() ? null : tendencyProgression,
				images.isEmpty() ? null : images),
			new AvalancheBulletinCustomData.LwdTyrol(dangerPatterns)
		));
		bulletin.setDangerRatings(getDangerRatings(avalancheBulletin).toList());
		bulletin.setHighlights(avalancheBulletin.getHighlightsIn(lang).orElse(null));
		bulletin.setLang(lang.name());
		bulletin.setMetaData(null);
		bulletin.setPublicationTime(avalancheBulletin.getPublicationDate().toInstant().truncatedTo(ChronoUnit.SECONDS));
		bulletin.setRegions(avalancheBulletin.getPublishedRegions().stream()
			.map(id -> new org.caaml.v6.Region(id, lang.getRegionName(id)))
			.toList());
		bulletin.setSnowpackStructure(org.caaml.v6.Texts.of(avalancheBulletin.getSnowpackStructureHighlightsIn(lang), avalancheBulletin.getSnowpackStructureCommentIn(lang)));
		bulletin.setSource(null);
		bulletin.setTendency(List.of(new Tendency(
			avalancheBulletin.getTendencyCommentIn(lang).orElse(null),
			avalancheBulletin.getTendency() != null ? TendencyType.forValue(avalancheBulletin.getTendency().name()) : null,
			new ValidTime(avalancheBulletin.getValidFrom().plusDays(1).toInstant(), avalancheBulletin.getValidUntil().plusDays(1).toInstant())
		)));
		bulletin.setTravelAdvisory(org.caaml.v6.Texts.of(avalancheBulletin.getTravelAdvisoryHighlightsIn(lang), avalancheBulletin.getTravelAdvisoryCommentIn(lang)));
		bulletin.setValidTime(new ValidTime(avalancheBulletin.getValidFrom().toInstant(), avalancheBulletin.getValidUntil().toInstant()));
		bulletin.setWeatherForecast(org.caaml.v6.Texts.of(avalancheBulletin.getSynopsisHighlightsIn(lang), avalancheBulletin.getSynopsisCommentIn(lang)));
		return bulletin;
	}

	private static Stream<org.caaml.v6.DangerRating> getDangerRatings(AvalancheBulletin avalancheBulletin) {
		return (avalancheBulletin.isHasDaytimeDependency()
			? Stream.of(avalancheBulletin.getForenoon(), avalancheBulletin.getAfternoon())
			: Stream.of(avalancheBulletin.getForenoon()))
			.flatMap(daytime -> Stream.of(
				getDangerRating(avalancheBulletin, daytime, daytime.dangerRating(false)),
				getDangerRating(avalancheBulletin, daytime, daytime.dangerRating(true))))
			.distinct();
	}

	private static AvalancheBulletinCustomData.BulletinPhoto toCaamlBulletinPhoto(AvalancheBulletinPhoto photo) {
		return new AvalancheBulletinCustomData.BulletinPhoto(
			photo.getUrl(),
			Strings.emptyToNull(photo.getCopyright()),
			photo.getDate(),
			Strings.emptyToNull(photo.getMicroRegionId()),
			Strings.emptyToNull(photo.getLocationName()),
			photo.getLatitude(),
			photo.getLongitude());
	}

	private static org.caaml.v6.AvalancheProblem getAvalancheProblem(AvalancheProblem p, ValidTimePeriod validTimePeriod) {
		if (p == null || p.getAvalancheProblem() == null) {
			return null;
		}
		final org.caaml.v6.AvalancheProblem result = new org.caaml.v6.AvalancheProblem();
		result.setAspects(p.getAspects().stream().map(a -> org.caaml.v6.Aspect.forValue(a.name())).toList());
		result.setProblemType(AvalancheProblemType.forValue(p.getAvalancheProblem().toString()));
		final String lowerBound = p.getElevationLow() > 0 ? Integer.toString(p.getElevationLow()) : p.getTreelineLow() ? "treeline" : null;
		final String upperBound = p.getElevationHigh() > 0 ? Integer.toString(p.getElevationHigh()) : p.getTreelineHigh() ? "treeline" : null;
		result.setElevation(new ElevationBoundaryOrBand(lowerBound, upperBound));
		result.setValidTimePeriod(validTimePeriod);
		final EawsMatrixInformation matrixInformation = p.getEawsMatrixInformation();
		if (matrixInformation != null) {
			DangerRating rating = matrixInformation.getDangerRating();
			if (rating != null) {
				result.setDangerRatingValue(DangerRatingValue.forValue(rating.name()));
			}
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

	private static org.caaml.v6.DangerRating getDangerRating(AvalancheBulletin avalancheBulletin, AvalancheBulletinDaytimeDescription daytime, DangerRating rating) {
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
			result.setValidTimePeriod(ValidTimePeriod.all_day);
		} else if (daytime == avalancheBulletin.getForenoon()) {
			result.setValidTimePeriod(ValidTimePeriod.earlier);
		} else if (daytime == avalancheBulletin.getAfternoon()) {
			result.setValidTimePeriod(ValidTimePeriod.later);
		} else {
			result.setValidTimePeriod(null);
		}
		return result;
	}
}
