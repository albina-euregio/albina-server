# CAAML / EAWS matrix i18n bundle

Consolidated translations for the EAWS avalanche-bulletin matrix vocabulary
(`avalancheSize`, `snowpackStability`, `frequency`, `dangerRating`,
`avalancheProblem`, `dangerPattern`, `aspect`, `tendency`), one JSON file per
language (`de`, `it`, `en`, `fr`, `es`, `ca`, `oc`, `sl`).

These terms were previously translated separately in albina-website,
albina-server and albina-admin-gui. This bundle is a first step towards a
single shared source of truth that can be published from `albina-caaml` and
consumed by all three projects.

Keys match the corresponding Java enum constants in
`eu.albina.model.enumerations` (e.g. `AvalancheSize`, `SnowpackStability`,
`Frequency`, `DangerRating`, `AvalancheProblem`, `DangerPattern`, `Aspect`,
`Tendency`). Suffixes `.long` and `.speech` denote alternate renderings
(long-form label, text-to-speech phrasing) for the same value, where they
exist.

Generated from the existing XML resource bundles in the parent `i18n/`
directory (`Aspect*.xml`, `AvalancheProblem*.xml`, `DangerPattern*.xml`,
`DangerRating*.xml`, `Tendency*.xml`, `MessagesBundle*.xml`), which remain
the values actually consumed by this project (e.g. `PdfUtil`). Keep both in
sync until the code is migrated to read from this bundle directly.
