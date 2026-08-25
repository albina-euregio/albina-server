# CAAML / EAWS matrix i18n bundle

Consolidated translations for EAWS avalanche-bulletin vocabulary
(`avalancheSize`, `snowpackStability`, `frequency`, `dangerRating`,
`avalancheProblem`, `dangerPattern`, `aspect`, `tendency`, `validTimePeriod`,
`elevation`, plus a few standalone report labels), one flat JSON file per
language (`de`, `it`, `en`, `fr`, `es`, `ca`, `oc`, `sl`).

These terms were previously translated separately in albina-website,
albina-server and albina-admin-gui. This bundle is a first step towards a
single shared source of truth that can be published from `albina-caaml` and
consumed by all three projects.

Format: flat key-value pairs (Transifex `KEYVALUEJSON`), keyed with dot
notation, e.g. `dangerRating.low`, `dangerRating.low.long`. The prefix before
the first dot mostly matches the corresponding Java enum in
`eu.albina.model.enumerations` (`AvalancheSize`, `SnowpackStability`,
`Frequency`, `DangerRating`, `AvalancheProblem`, `DangerPattern`, `Aspect`,
`Tendency`); each such concept also carries a `<concept>.label` entry for its
section heading. Suffixes `.long` and `.speech` denote alternate renderings
(long-form label, text-to-speech phrasing) where they exist.

This is the bundle actually loaded by the Java code at runtime (see
`eu.albina.util.XMLResourceBundleControl`, baseName `i18n.caaml`, accessed via
`LanguageCode.getCaamlBundleString(key)`), not just a static export.
