package eu.albina.model;

import java.util.Map;
import java.util.UUID;

public interface Incidents {

	record IncidentSchema(
		AccidentalControlled accidentalControlled,
		AdditionalLoad additionalLoad,
		Attachment[] attachments,
		String author,
		String authorAffiliation,
		Map<String, String> avalancheDescription,
		Boolean avalancheDescriptionPublic,
		String avalancheDetailsComment,
		Double avalancheLength,
		AvalancheProblem[] avalancheProblems,
		String avalancheRegion,
		IncidentSchemaAvalancheSize avalancheSize,
		AvalancheType avalancheType,
		BedSurfaceStepped bedSurfaceStepped,
		String bulletinInformationComment,
		String country,
		CriticalWarming criticalWarming,
		Double crownDepthAvg,
		Double crownDepthMax,
		Double crownDepthMin,
		String[] damagedAssets,
		DangerPattern[] dangerPattern,
		DangerRating dangerRating,
		Object dateTime,
		Double debrisDensity,
		String[] debrisType,
		Double depositElevation,
		Double depositHeight,
		DepositMoisture depositMoisture,
		Double depositWidth,
		String explosives,
		String generalInformationComment,
		GroupInformation[] groupInformation,
		UUID id,
		String incidentAnalysisComment,
		Map<String, String> incidentDescription,
		Boolean incidentDescriptionPublic,
		Map<String, String> incidentLede,
		Boolean incidentLedePublic,
		InvolvementsFatalitiesBurials involvementsFatalitiesBurials,
		double latitude,
		String lineCoordinatesText,
		String location,
		LocationAccuracy locationAccuracy,
		double longitude,
		BedSurfaceStepped multipleAvalanches,
		String municipality,
		Natural natural,
		BedSurfaceStepped otherDamages,
		String otherDamagesComment,
		Person person,
		PersonInvolvement personInvolvement,
		String polygonCoordinatesText,
		String privateExternalDatabaseLinks,
		String privateExternalLinks,
		String publicAvalancheWarningService,
		Boolean publicAvalancheWarningServiceOutside,
		String publicExternalLinks,
		Object publishedAt,
		CriticalWarming recentLoading,
		CriticalWarming recentSlabAvalanches,
		String region,
		ProblemType relevantAvalancheProblem,
		BedSurfaceStepped remoteTriggering,
		ReportStatus reportStatus,
		CriticalWarming signsOfInstability,
		Double slabWidth,
		Map<String, String> snowpackDescription,
		Boolean snowpackDescriptionPublic,
		String[] sourceOfInformation,
		StartZoneAspect startZoneAspect,
		StartZoneAspectAccuracy startZoneAspectAccuracy,
		Double startZoneElevation,
		StartZoneElevationAccuracy startZoneElevationAccuracy,
		Double startZoneIncline,
		StartZoneMoisture startZoneMoisture,
		String[] startZoneTerrainType,
		Map<String, String> takeAways,
		Boolean takeAwaysPublic,
		TimeAccuracy timeAccuracy,
		String trigger,
		Object updatedAt,
		String vehicle,
		VictimInformation[] victimInformation,
		Double weakLayerGrainSize1,
		Double weakLayerGrainSize2,
		WeakLayerGrainType weakLayerGrainType1,
		WeakLayerGrainType weakLayerGrainType2,
		WeakLayerLocation weakLayerLocation,
		String weakLayerName,
		Map<String, String> weatherDescription,
		Boolean weatherDescriptionPublic
	) {
	}

	enum AccidentalControlled {
		Accidental, Controlled
	}

	enum AdditionalLoad {
		High, Low
	}

	record Attachment(
		String altText,
		AttachmentCategory attachmentCategory,
		String[] attachmentTags,
		String caption,
		String credit,
		Object dateAdded,
		Object dateCreated,
		String file,
		String fileName,
		UUID id,
		String mediaType,
		Boolean attachmentPublic
	) {
	}

	enum AttachmentCategory {
		Avalanche, Group, Incident, Person, Snowpack, Weather
	}

	record AvalancheProblem(
		StartZoneAspect[] aspects,
		AvalancheProblemAvalancheSize avalancheSize,
		String elevationLowerBound,
		String elevationUpperBound,
		Frequency frequency,
		ProblemType problemType,
		SnowpackStability snowpackStability
	) {
	}

	enum StartZoneAspect {
		E, N, NE, NW, S, SE, SW, W
	}

	enum AvalancheProblemAvalancheSize {
		extreme, large, medium, small, very_large
	}

	enum Frequency {
		few, many, none, some
	}

	enum ProblemType {
		cornices, gliding_snow, new_snow, no_distinct_avalanche_problem, persistent_weak_layers, wet_snow, wind_slab
	}

	enum SnowpackStability {
		fair, good, poor, very_poor
	}

	enum IncidentSchemaAvalancheSize {
		extreme, large, large_very_large, medium, medium_large, small, small_medium, unknown, very_large, very_large_extreme
	}

	enum AvalancheType {
		cornice, glide, loose, slab, unknown
	}

	enum BedSurfaceStepped {
		No, Yes
	}

	enum CriticalWarming {
		Absent, Present, Unknown
	}

	enum DangerPattern {
		dp1, dp10, dp2, dp3, dp4, dp5, dp6, dp7, dp8, dp9
	}

	enum DangerRating {
		considerable, high, low, moderate, no_rating, no_snow, very_high
	}

	enum DepositMoisture {
		Dry, Moist, Wet
	}

	record GroupInformation(
		String anonymousGroupIdentifier,
		AvalancheGear avalancheGear,
		String groupInformationComment,
		Double groupSize,
		GroupSizeAccuracy groupSizeAccuracy,
		String groupType,
		UUID id,
		String incidentActivity,
		IncidentTerrainType incidentTerrainType,
		String travelDirection,
		String typeOfControlledTerrain,
		String vehicleType
	) {
	}

	enum AvalancheGear {
		All, None, Some, Unknown
	}

	enum GroupSizeAccuracy {
		Approximately, AtLeast, Exact, Unknown
	}

	enum IncidentTerrainType {
		ControlledTerrainClosed, ControlledTerrainOpen, FreeTerrain, Unknown
	}

	record InvolvementsFatalitiesBurials(
		Double caughtOnly,
		Double fatalities,
		Double fullyBuried,
		String[] incidentActivity,
		IncidentTerrainType[] incidentTerrainType,
		Double injuredSurvivors,
		String involvementsFatalitiesBurialsComment,
		Double numberInvolved,
		Double numberOfGroups,
		Double partlyBuried,
		Double partlyBuriedHeadCovered,
		Double partlyBuriedHeadUncovered,
		Double uninjuredSurvivors
	) {
	}

	enum LocationAccuracy {
		exact, unknown, within100m, within10km, within15m, within1km, within20km, within250m, within2km, within30m, within500m, within50km, within5km
	}

	enum Natural {
		CorniceFall, Earthquake, IceFall, Natural, RockFall
	}

	enum Person {
		PersonAccidental, PersonControlled
	}

	enum PersonInvolvement {
		No, Unknown, Yes
	}

	enum ReportStatus {
		Draft, InReview, Incomplete, Verified
	}

	enum StartZoneAspectAccuracy {
		Accurate, Uncertain
	}

	enum StartZoneElevationAccuracy {
		exact, unknown, within100m, within200m, within50m
	}

	enum StartZoneMoisture {
		Dry, Moist, Unknown, Wet
	}

	enum TimeAccuracy {
		P1D, P2D, P3D, PT12H, PT15M, PT1H, PT2H, PT30M, PT4H, PT6H, exact, unknown
	}

	record VictimInformation(
		Age age,
		Airbag airbag,
		String anonymousVictimIdentifier,
		String avalancheTraining,
		BurialDegree burialDegree,
		Double burialDepth,
		Double burialDuration,
		Caught caught,
		String causeOfDeath,
		String country,
		EstimatedTimeOfDeath estimatedTimeOfDeath,
		FatalInjured fatalInjured,
		Gender gender,
		UUID groupID,
		BedSurfaceStepped helmet,
		UUID id,
		InjurySeverity injurySeverity,
		BedSurfaceStepped leaderAtTime,
		String medicalIntervention,
		String primaryLocationMethod,
		BedSurfaceStepped probe,
		String professionalCertification,
		String rescuedBy,
		BedSurfaceStepped respiratoryCavity,
		BedSurfaceStepped shovel,
		String terrainTrap,
		Transceiver transceiver,
		String victimInformationComment,
		BedSurfaceStepped workingAtTime,
		YearsActive yearsActive
	) {
	}

	enum Age {
		From14To20, From21To30, From31To40, From41To50, From51To60, From61To70, From71, UpTo13
	}

	enum Airbag {
		AirbagDeployed, AirbagUndeployed, NoAirbag
	}

	enum BurialDegree {
		FullyBuried, NotBuried, PartlyBuried, PartlyBuriedHeadCovered, PartlyBuriedHeadUncovered, Unknown
	}

	enum Caught {
		Involved, NotInvolved, Unknown
	}

	enum EstimatedTimeOfDeath {
		DuringBurial, DuringTheAvalanche, DuringTransport, InHospital, OnSiteAfterExtrication
	}

	enum FatalInjured {
		Fatal, Injured, Uninjured, Unknown
	}

	enum Gender {
		Female, Male, Other
	}

	enum InjurySeverity {
		Major, Minor, Moderate
	}

	enum Transceiver {
		NoTransceiver, TransceiverOff, TransceiverOn
	}

	enum YearsActive {
		From10Years, From3To9Years, UpTo2Years
	}

	enum WeakLayerGrainType {
		DF, DH, FC, FCxr, MF, MM, PP, PPgp, RG, SH
	}

	enum WeakLayerLocation {
		AtInterfaceWithOldSnow, NearTheGround, WithinNewSnow, WithinOldSnowpack
	}

}
