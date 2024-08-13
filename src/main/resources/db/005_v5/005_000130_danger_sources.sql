-- liquibase formatted sql
-- changeset albina:000_000050 failOnError:true

create table danger_source_variant_danger_signs (DANGER_SIGN tinyint check (DANGER_SIGN between 0 and 1), DANGER_SOURCE_VARIANT_ID varchar(255) not null) engine=InnoDB;
create table danger_source_variant_terrain_types (TERRAIN_TYPE tinyint check (TERRAIN_TYPE between 0 and 1), DANGER_SOURCE_VARIANT_ID varchar(255) not null) engine=InnoDB;
create table danger_source_variant_aspects (ASPECT tinyint check (ASPECT between 0 and 7), DANGER_SOURCE_VARIANT_ID varchar(255) not null) engine=InnoDB;
create table danger_source_variant_regions (DANGER_SOURCE_VARIANT_ID varchar(255) not null, REGION_ID varchar(255)) engine=InnoDB;
create table danger_source_variants (AVALANCHE_SIZE_VALUE integer, DANGER_INCREASE_WITH_ELEVATION bit, ELEVATION_HIGH integer, ELEVATION_LOW integer, FREQUENCY_VALUE integer, HAS_DAYTIME_DEPENDENCY bit, REMOTE_TRIGGERING bit, RUNOUT_INTO_GREEN bit, SLAB_THICKNESS_LOWER_LIMIT integer, SLAB_THICKNESS_UPPER_LIMIT integer, SNOWPACK_STABILITY_VALUE integer, SNOW_HEIGHT_LOWER_LIMIT integer, SNOW_HEIGHT_UPPER_LIMIT integer, TREELINE_HIGH bit, TREELINE_LOW bit, WEAK_LAYER_CRUST_ABOVE bit, WEAK_LAYER_CRUST_BELOW bit, WEAK_LAYER_GRAIN_SIZE_LOWER_LIMIT float(53), WEAK_LAYER_GRAIN_SIZE_UPPER_LIMIT float(53), WEAK_LAYER_PERSISTENT bit, WEAK_LAYER_WET bit, ZERO_DEGREE_ISOTHERM bit, CREATION_DATE datetime(6), UPDATE_DATE datetime(6), VALID_FROM datetime(6), VALID_UNTIL datetime(6), DANGER_SOURCE_ID varchar(255), DANGER_SOURCE_VARIANT_ID varchar(255), ID varchar(255) not null, OWNER_REGION varchar(255), AVALANCHE_SIZE enum ('extreme','large','medium','small','very_large'), AVALANCHE_TYPE enum ('glide','loose','slab'), DANGER_PEAK enum ('afternnon','evening','first_night_half','forenoon','morning','second_night_half'), DANGER_RATING enum ('considerable','high','low','missing','moderate','no_rating','no_snow','very_high'), DANGER_RATING_MODIFICATOR enum ('equal','minus','plus'), DANGER_SPOT_RECOGNIZABILITY enum ('easy','hard','very_easy','very_hard'), FREQUENCY enum ('few','many','none','some'), GLIDING_SNOW_ACTIVITY enum ('high','low','medium'), HIGHEST_DANGER_ASPECT enum ('E','N','NE','NW','S','SE','SW','W'), LOOSE_SNOW_GRAIN_SHAPE enum ('DF','DFbk','DFdc','DH','DHch','DHcp','DHla','DHpr','DHxr','FC','FCsf','FCso','FCxr','IF','IFbi','IFic','IFil','IFrc','IFsc','MF','MFcl','MFcr','MFpc','MFsl','MM','MMci','MMrp','PP','PPco','PPgp','PPhl','PPip','PPir','PPnd','PPpl','PPrm','PPsd','RG','RGlr','RGsr','RGwp','RGxf','SH','SHcv','SHsu','SHxr'), LOOSE_SNOW_MOISTURE enum ('dry','moist','wet'), NATURAL_RELEASE enum ('likely','possible','unlikely'), SLAB_DISTRIBUTION enum ('isolated','specific','widespread'), SLAB_ENERGY_TRANSFER_POTENTIAL enum ('low', 'medium', 'high', 'very_high'), SLAB_GRAIN_SHAPE enum ('DF','DFbk','DFdc','DH','DHch','DHcp','DHla','DHpr','DHxr','FC','FCsf','FCso','FCxr','IF','IFbi','IFic','IFil','IFrc','IFsc','MF','MFcl','MFcr','MFpc','MFsl','MM','MMci','MMrp','PP','PPco','PPgp','PPhl','PPip','PPir','PPnd','PPpl','PPrm','PPsd','RG','RGlr','RGsr','RGwp','RGxf','SH','SHcv','SHsu','SHxr'), SLAB_HAND_HARDNESS_LOWER_LIMIT enum ('fist','four_fingers','ice','knife','one_finger','pencil'), SLAB_HAND_HARDNESS_UPPER_LIMIT enum ('fist','four_fingers','ice','knife','one_finger','pencil'), SLAB_HARDNESS_PROFILE enum ('decreasing','increasing','steady'), SLOPE_GRADIENT enum ('extremely_steep','moderately_steep','steep','very_steep'), SNOWPACK_STABILITY enum ('fair','good','poor','very_poor'), STATUS enum ('activ','dormant','inactive'), TYPE enum ('analysis','forecast'), WEAK_LAYER_CREATION enum ('diurnal_recrystallization','melt_layer_recrystallization','radiation_recrystallization'), WEAK_LAYER_DISTRIBUTION enum ('isolated','specific','widespread'), WEAK_LAYER_GRAIN_SHAPE enum ('DF','DFbk','DFdc','DH','DHch','DHcp','DHla','DHpr','DHxr','FC','FCsf','FCso','FCxr','IF','IFbi','IFic','IFil','IFrc','IFsc','MF','MFcl','MFcr','MFpc','MFsl','MM','MMci','MMrp','PP','PPco','PPgp','PPhl','PPip','PPir','PPnd','PPpl','PPrm','PPsd','RG','RGlr','RGsr','RGwp','RGxf','SH','SHcv','SHsu','SHxr'), WEAK_LAYER_POSTION enum ('ground','lower','middle','upper'), WEAK_LAYER_STRENGTH enum ('high','low','medium','very_high'), WEAK_LAYER_THICKNESS enum ('thick','thin'), primary key (ID)) engine=InnoDB;
create table danger_sources (CREATION_DATE datetime(6), DESCRIPTION varchar(255), TITLE varchar(255), ID varchar(255) not null, primary key (ID)) engine=InnoDB;
