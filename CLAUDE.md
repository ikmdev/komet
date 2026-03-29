# Komet

JavaFX-based knowledge management application framework.

## Build Standards

Files in `.claude/standards/` are build artifacts unpacked from `ike-build-standards`. DO NOT edit or commit them. See the workspace root CLAUDE.md for details.

## Build

```bash
mvn clean verify -DskipTests -T4
```

## Key Facts

- GroupId: `dev.ikm.komet`
- Parent: `network.ike:ike-parent`
- Uses `--enable-preview` (Java 25)
- BOM: imports `dev.ikm.ike:ike-bom`
- 18 submodules using `<subprojects>` aggregation
