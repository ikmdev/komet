# komet

komet subproject.

## Build Standards

Files in `.claude/standards/` are build artifacts unpacked from `ike-build-standards`. DO NOT edit or commit them. See the workspace root CLAUDE.md for details.

## Build

```bash
mvn clean verify -DskipTests -T4
```

## Key Facts

- GroupId: `dev.ikm.komet`
- Version: `1.59.0-SNAPSHOT`
- Uses `--enable-preview` (Java 25)
- BOM: imports `dev.ikm.ike:ike-bom` for dependency version management

## Prohibited Patterns

- **Never use `maven-antrun-plugin`** — use a proper Maven goal or `exec-maven-plugin`
- **Never use `build-helper-maven-plugin` for multi-execution property chaining** —
  write a proper Maven goal in `ike-maven-plugin`
- **Never embed shell commands inline in POM** — extract to a named script

See `.claude/standards/` (after `mvn validate`) for full standards.
See `CLAUDE-komet.md` for project-specific notes.
<!-- BEGIN ike-managed: standards-pointer -->

## IKE Build Standards

This project follows the IKE build standards. Run `mvn validate` to
unpack them into `.claude/standards/` — build artifacts from
`ike-build-standards`, so **do not edit or commit them** — then read and
follow them (start with `MAVEN.md` and `IKE-MAVEN.md`).

Diagrams on web pages (`src/site/asciidoc/`) follow `IKE-DIAGRAMS.md`:
pre-render to committed static SVG under `src/site/resources/images/` and
reference with `image::` — never inline `[plantuml]`/`[graphviz]` blocks
or live Kroki URLs (the Maven site parser does not render them).
<!-- END ike-managed: standards-pointer -->
