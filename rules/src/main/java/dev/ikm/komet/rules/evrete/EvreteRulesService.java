/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.komet.rules.evrete;

import dev.ikm.komet.framework.performance.Statement;
import dev.ikm.komet.framework.rulebase.Consequence;
import dev.ikm.komet.framework.rulebase.ListedRuleSet;
import dev.ikm.komet.framework.rulebase.RuleFinding;
import dev.ikm.komet.framework.rulebase.RuleProvider;
import dev.ikm.komet.framework.rulebase.RuleService;
import dev.ikm.komet.framework.rulebase.RuleSource;
import dev.ikm.komet.framework.view.ViewProperties;
import dev.ikm.komet.rules.annotated.AxiomFocusedRules;
import dev.ikm.komet.rules.annotated.ComponentFocusRules;
import dev.ikm.komet.rules.annotated.NewConceptRules;
import dev.ikm.komet.rules.annotated.NewPatternRules;
import dev.ikm.tinkar.common.service.PluggableService;
import dev.ikm.tinkar.common.sets.ConcurrentHashSet;
import dev.ikm.tinkar.coordinate.edit.EditCoordinate;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.evrete.Configuration;
import org.evrete.KnowledgeService;
import org.evrete.api.ActivationMode;
import org.evrete.api.Knowledge;
import org.evrete.api.RuleDescriptor;
import org.evrete.api.StatelessSession;
import org.evrete.api.spi.SourceCompiler;
import org.evrete.dsl.Constants;
import org.evrete.dsl.annotation.RuleSet;
import org.evrete.util.CompilationException;
import org.evrete.util.JavaSourceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Komet's rules on the Evrete rule engine.
 *
 * <p>Komet's own rule classes and the rule classes plugins bring are read once, when the service
 * starts. A {@link RuleSource} is compiled when it is put in use, by the rule engine's own
 * compiler and against the types this class itself can see, and the rules are then built again
 * from every rule set fit to take part. The build is replaced whole, so a firing under way
 * finishes on the rules it started with. A proposed rule source is compiled and listed, and
 * takes no part in the build until a person accepts it.
 *
 * <p>The rule engine is told not to take literal data: a condition written as text would be
 * compiled each time the rules are built, which is what made starting Komet slow. A rule's
 * conditions are therefore methods, in Komet's own rules and in a rule source alike.
 */
public class EvreteRulesService implements RuleService {
    /** The session variable the rules add their consequences to. */
    public static final String ENV_CONSEQUENCES = "ENV_CONSEQUENCES";
    /** The session variable for the view the rules read the knowledge base through. */
    public static final String ENV_VIEW_PROPERTIES = "ENV_VIEW_PROPERTIES";
    /** The session variable for the author, module and path a rule's action would write under. */
    public static final String ENV_EDIT_COORDINATE = "ENV_EDIT_COORDINATE";

    private static final Logger LOG = LoggerFactory.getLogger(EvreteRulesService.class);

    private static final List<Class<?>> KOMET_RULE_CLASSES = List.of(
            ComponentFocusRules.class,
            NewConceptRules.class,
            AxiomFocusedRules.class,
            NewPatternRules.class);

    /** How the compiler reports one refused line: {@code <file>:<class>:<line>: error: <message>}. */
    private static final Pattern COMPILER_LINE =
            Pattern.compile("\\A[^:\\s]+:[^:\\s]+:(\\d+): (?:error: )?(.*)\\z", Pattern.DOTALL);

    private static final String LITERAL_DATA_REFUSED = "Literal conditions";

    private final KnowledgeService service;
    /** Komet's own rule sets, then the plugins': they do not change while Komet runs. */
    private final ImmutableList<Candidate> fixedRuleSets;
    /**
     * The accepted rule sources in the order they first came. Its monitor guards it, the proposed
     * rule sources and every rebuild.
     */
    private final Map<UUID, Candidate> sourceRuleSets = new LinkedHashMap<>();
    /** The proposed rule sources, compiled and waiting for a person to accept them. */
    private final Map<UUID, Candidate> proposedRuleSets = new LinkedHashMap<>();
    private volatile InUse inUse;

    /**
     * A rule set before it is listed: its classes and the names of their rules when it is fit to
     * take part, or the findings that keep it out.
     */
    private record Candidate(ListedRuleSet.Origin origin,
                             String name,
                             List<Class<?>> classes,
                             ImmutableList<String> ruleNames,
                             ImmutableList<RuleFinding> findings,
                             Optional<RuleSource> source) {

        boolean fit() {
            return findings.isEmpty();
        }

        Candidate refused(RuleFinding finding) {
            return new Candidate(origin, name, classes, ruleNames, Lists.immutable.of(finding), source);
        }

        ListedRuleSet listed(boolean accepted) {
            return new ListedRuleSet(origin, name, ruleNames, accepted, accepted && fit(), findings, source);
        }

        boolean hasIdentity(UUID id) {
            return source.map(RuleSource::id).filter(id::equals).isPresent();
        }
    }

    /** The rules as built, and every rule set listed as that build left it. */
    private record InUse(Knowledge knowledge, ImmutableList<ListedRuleSet> listed) {
    }

    /**
     * Starts the rule engine and reads Komet's own rules and the plugins'.
     *
     * @throws IOException when one of Komet's own rule classes cannot be read
     */
    public EvreteRulesService() throws IOException {
        Instant t0 = Instant.now();
        Configuration conf = new Configuration();

        // Literal data requires the Java compiler and significantly increases build time.
        // Setting this option will fast-fail the engine in case of a literal condition/action.
        conf.set(Configuration.DISABLE_LITERAL_DATA, "true");

        for (Map.Entry<Object, Object> confEntry : conf.entrySet()) {
            LOG.info(confEntry.toString());
        }

        this.service = new KnowledgeService(conf);

        MutableList<Candidate> fixed = Lists.mutable.empty();
        // Komet's own rules always load: a failure here is a defect in Komet and stops the service.
        for (Class<?> ruleClass : KOMET_RULE_CLASSES) {
            fixed.add(new Candidate(ListedRuleSet.Origin.KOMET, nameOf(ruleClass), List.of(ruleClass),
                    ruleNamesOf(List.of(ruleClass)), Lists.immutable.empty(), Optional.empty()));
        }
        // Each plugin's rule classes are read on their own, so one bad plugin rule set is listed
        // out of use instead of taking down the whole engine, and with it every rule-driven menu.
        int pluginRuleClassCount = 0;
        for (RuleProvider provider : PluggableService.load(RuleProvider.class)) {
            for (Candidate pluginRules : pluginRuleSets(provider)) {
                fixed.add(pluginRules);
                pluginRuleClassCount += pluginRules.fit() ? 1 : 0;
            }
        }
        if (pluginRuleClassCount > 0) {
            LOG.info("Imported {} plugin-contributed rule class(es) via RuleProvider", pluginRuleClassCount);
        }
        this.fixedRuleSets = fixed.toImmutable();
        this.inUse = build(List.of());
        Instant t1 = Instant.now();

        // Log the timing. With literal conditions disabled, the cold start time
        // is expected to be in the range of tens of milliseconds.
        LOG.info("Constructed EvreteRulesService, duration: {}ms", Duration.between(t0, t1).toMillis());
    }

    @Override
    public ImmutableList<Consequence<?>> execute(String knowledgeBaseName,
                                                 ImmutableList<Statement> statements,
                                                 ViewProperties viewProperties, EditCoordinate editCoordinate) {
        while (true) {
            InUse current = this.inUse;
            try {
                return fire(current.knowledge(), statements, viewProperties, editCoordinate);
            } catch (RuntimeException thrown) {
                // A rule source whose rule throws is taken out of use, and the others fire again.
                if (!tookOutOfUseTheSourceThatThrew(thrown, current)) {
                    throw thrown;
                }
            }
        }
    }

    @Override
    public ImmutableList<ListedRuleSet> ruleSets() {
        return this.inUse.listed();
    }

    @Override
    public ListedRuleSet checkRuleSource(RuleSource source) {
        Candidate candidate = compiled(source);
        synchronized (sourceRuleSets) {
            Map<UUID, Candidate> withIt = new LinkedHashMap<>(sourceRuleSets);
            withIt.put(source.id(), candidate);
            return listedSource(build(withIt.values()), source.id(), true);
        }
    }

    @Override
    public ListedRuleSet putRuleSource(RuleSource source) {
        Candidate candidate = compiled(source);
        synchronized (sourceRuleSets) {
            sourceRuleSets.put(source.id(), candidate);
            this.inUse = build(sourceRuleSets.values());
            return listedSource(this.inUse, source.id(), true);
        }
    }

    @Override
    public ListedRuleSet proposeRuleSource(RuleSource source) {
        // Compiling the source and having the rule engine read its class runs none of its code:
        // a rule class is first initialized when rules that include it are fired.
        Candidate candidate = compiled(source);
        synchronized (sourceRuleSets) {
            proposedRuleSets.put(source.id(), candidate);
            this.inUse = build(sourceRuleSets.values());
            return listedSource(this.inUse, source.id(), false);
        }
    }

    @Override
    public Optional<ListedRuleSet> acceptRuleSource(UUID id) {
        synchronized (sourceRuleSets) {
            Candidate proposed = proposedRuleSets.remove(id);
            if (proposed == null) {
                return Optional.empty();
            }
            sourceRuleSets.put(id, proposed);
            this.inUse = build(sourceRuleSets.values());
            return Optional.of(listedSource(this.inUse, id, true));
        }
    }

    @Override
    public boolean discardProposedRuleSource(UUID id) {
        synchronized (sourceRuleSets) {
            if (proposedRuleSets.remove(id) == null) {
                return false;
            }
            this.inUse = build(sourceRuleSets.values());
            return true;
        }
    }

    @Override
    public boolean removeRuleSource(UUID id) {
        synchronized (sourceRuleSets) {
            if (sourceRuleSets.remove(id) == null) {
                return false;
            }
            this.inUse = build(sourceRuleSets.values());
            return true;
        }
    }

    private static ImmutableList<Consequence<?>> fire(Knowledge knowledge,
                                                      ImmutableList<Statement> statements,
                                                      ViewProperties viewProperties,
                                                      EditCoordinate editCoordinate) {
        StatelessSession session = knowledge.newStatelessSession(ActivationMode.CONTINUOUS);

        // Create a collector for resulting consequences
        ConcurrentHashSet<Consequence<?>> globalActionSet = new ConcurrentHashSet<>();

        // Set the collector and other necessary objects as session's environment variables
        session.set(ENV_CONSEQUENCES, globalActionSet);
        session.set(ENV_VIEW_PROPERTIES, viewProperties);
        session.set(ENV_EDIT_COORDINATE, editCoordinate);

        // Insert the statements
        session.insert(statements.castToList());

        session.fire((handle, object) -> {
            // Inspect memory objects
            LOG.atDebug().log("handle: " + handle + " object: " + object);
        });

        LOG.atDebug().log("Set items: " + globalActionSet.stream().toList());

        MutableList<Consequence<?>> globalActionList = Lists.mutable.ofAll(globalActionSet);
        globalActionList.sort(Comparable::compareTo);
        return globalActionList.toImmutableList();
    }

    /**
     * Builds the rules from every rule set fit to take part, Komet's own first, then the
     * plugins', then the accepted rule sources, and lists them all as the build left them,
     * followed by the proposed rule sources, which take no part.
     */
    private InUse build(Collection<Candidate> sources) {
        // A rule set the engine refuses here, after taking it alone, may leave the build half
        // changed, so the build starts over without it.
        Map<Candidate, RuleFinding> refusedHere = new IdentityHashMap<>();
        while (true) {
            Optional<InUse> built = buildWithout(refusedHere, sources);
            if (built.isPresent()) {
                return built.get();
            }
        }
    }

    private Optional<InUse> buildWithout(Map<Candidate, RuleFinding> refusedHere, Collection<Candidate> sources) {
        Knowledge knowledge = service.newKnowledge();
        MutableList<ListedRuleSet> listed = Lists.mutable.empty();
        Map<String, Candidate> ruleSetOfRule = new HashMap<>();
        List<Candidate> all = new ArrayList<>(fixedRuleSets.castToList());
        all.addAll(sources);
        for (Candidate candidate : all) {
            Candidate judged = candidate;
            if (refusedHere.containsKey(candidate)) {
                judged = candidate.refused(refusedHere.get(candidate));
            } else if (candidate.fit()) {
                judged = clashOf(candidate, ruleSetOfRule, Optional.empty()).map(candidate::refused).orElse(candidate);
            }
            if (judged.fit()) {
                try {
                    knowledge.importRules(Constants.PROVIDER_JAVA_CLASS, judged.classes());
                } catch (IOException | RuntimeException | LinkageError refusal) {
                    if (candidate.origin() == ListedRuleSet.Origin.KOMET) {
                        throw new IllegalStateException("Komet's own rule set " + candidate.name()
                                + " is no longer taken by the rule engine", refusal);
                    }
                    refusedHere.put(candidate, new RuleFinding(0, inItsOwnWords(refusal)));
                    return Optional.empty();
                }
                for (String ruleName : judged.ruleNames()) {
                    ruleSetOfRule.put(ruleName, judged);
                }
            }
            listed.add(judged.listed(true));
        }
        // A proposed rule source is listed with what would keep it out of use were it accepted
        // now. A name it shares with the accepted source of its own identity is no clash: it
        // would take that source's place.
        for (Candidate proposed : proposedRuleSets.values()) {
            Optional<UUID> itsIdentity = proposed.source().map(RuleSource::id);
            Candidate judged = proposed.fit()
                    ? clashOf(proposed, ruleSetOfRule, itsIdentity).map(proposed::refused).orElse(proposed)
                    : proposed;
            listed.add(judged.listed(false));
        }
        return Optional.of(new InUse(knowledge, listed.toImmutable()));
    }

    /**
     * Rule names are one of a kind across every rule set: the rule engine keeps its rules by name.
     *
     * @param replaces the identity of the rule source the candidate would take the place of
     */
    private static Optional<RuleFinding> clashOf(Candidate candidate, Map<String, Candidate> ruleSetOfRule,
                                                 Optional<UUID> replaces) {
        return candidate.ruleNames()
                .detectOptional(ruleName -> ruleSetOfRule.containsKey(ruleName)
                        && replaces.filter(ruleSetOfRule.get(ruleName)::hasIdentity).isEmpty())
                .map(ruleName -> new RuleFinding(0, "A rule named \"" + ruleName + "\" is already in use, in the rule set \""
                        + ruleSetOfRule.get(ruleName).name() + "\". Give this rule a name of its own in its @Rule annotation."));
    }

    /** Compiles a rule source and has the rule engine take its class alone, to learn its rules or its findings. */
    private Candidate compiled(RuleSource source) {
        try {
            SourceCompiler compiler = service.getSourceCompilerProvider()
                    .instance(EvreteRulesService.class.getClassLoader());
            SourceCompiler.ClassSource parsed = JavaSourceUtils.parse(source.java());
            Collection<SourceCompiler.Result<SourceCompiler.ClassSource>> results = compiler.compile(List.of(parsed));
            List<Class<?>> classes = new ArrayList<>();
            for (SourceCompiler.Result<SourceCompiler.ClassSource> result : results) {
                classes.add(result.getCompiledClass());
            }
            return new Candidate(ListedRuleSet.Origin.SOURCE, source.name(), List.copyOf(classes),
                    ruleNamesOf(classes), Lists.immutable.empty(), Optional.of(source));
        } catch (CompilationException refusal) {
            return refusedSource(source, findingsOf(refusal));
        } catch (IOException | RuntimeException | LinkageError refusal) {
            // Text that is not Java, or a class the rule engine will not take as rules.
            return refusedSource(source, Lists.immutable.of(new RuleFinding(0, inItsOwnWords(refusal))));
        }
    }

    private static Candidate refusedSource(RuleSource source, ImmutableList<RuleFinding> findings) {
        return new Candidate(ListedRuleSet.Origin.SOURCE, source.name(), List.of(),
                Lists.immutable.empty(), findings, Optional.of(source));
    }

    private List<Candidate> pluginRuleSets(RuleProvider provider) {
        String providerName = provider.getClass().getName();
        List<Candidate> ruleSets = new ArrayList<>();
        try {
            for (Class<?> ruleClass : provider.ruleClasses()) {
                ruleSets.add(pluginRuleSet(providerName, ruleClass));
            }
        } catch (RuntimeException | LinkageError refusal) {
            LOG.error("Skipping rules from RuleProvider {} — it could not name its rule classes; "
                    + "the rule engine continues without them: {}", providerName, refusal.toString());
            ruleSets.add(new Candidate(ListedRuleSet.Origin.PLUGIN, providerName, List.of(), Lists.immutable.empty(),
                    Lists.immutable.of(new RuleFinding(0, inItsOwnWords(refusal))), Optional.empty()));
        }
        return ruleSets;
    }

    private Candidate pluginRuleSet(String providerName, Class<?> ruleClass) {
        try {
            return new Candidate(ListedRuleSet.Origin.PLUGIN, nameOf(ruleClass), List.of(ruleClass),
                    ruleNamesOf(List.of(ruleClass)), Lists.immutable.empty(), Optional.empty());
        } catch (IOException | RuntimeException | LinkageError refusal) {
            LOG.error("Skipping rules from RuleProvider {} — import of {} failed; the rule "
                    + "engine continues without them: {}", providerName, ruleClass.getName(), refusal.toString());
            return new Candidate(ListedRuleSet.Origin.PLUGIN, nameOf(ruleClass), List.of(), Lists.immutable.empty(),
                    Lists.immutable.of(new RuleFinding(0, inItsOwnWords(refusal))), Optional.empty());
        }
    }

    /** Has the rule engine take the classes alone, and answers the names of the rules it found in them. */
    private ImmutableList<String> ruleNamesOf(List<Class<?>> ruleClasses) throws IOException {
        Knowledge alone = service.newKnowledge().importRules(Constants.PROVIDER_JAVA_CLASS, ruleClasses);
        MutableList<String> names = Lists.mutable.empty();
        for (RuleDescriptor rule : alone.getRules()) {
            names.add(rule.getName());
        }
        return names.toImmutable();
    }

    private static String nameOf(Class<?> ruleClass) {
        RuleSet ruleSet = ruleClass.getAnnotation(RuleSet.class);
        return ruleSet == null || ruleSet.value().isBlank() ? ruleClass.getSimpleName() : ruleSet.value();
    }

    private static ListedRuleSet listedSource(InUse built, UUID id, boolean accepted) {
        return built.listed()
                .detectOptional(ruleSet -> ruleSet.accepted() == accepted
                        && ruleSet.source().map(RuleSource::id).filter(id::equals).isPresent())
                .orElseThrow(() -> new IllegalStateException("The rule source " + id + " is not listed"));
    }

    /**
     * Finds the rule source one of whose classes is on the thrown exception's stack, keeps it out
     * of use with that as its finding, and builds the rules again.
     *
     * @return whether the rules are worth firing again: a source was taken out of use, or the
     *         rules had already been built again by someone else
     */
    private boolean tookOutOfUseTheSourceThatThrew(RuntimeException thrown, InUse firedOn) {
        synchronized (sourceRuleSets) {
            if (this.inUse != firedOn) {
                return true;
            }
            for (Map.Entry<UUID, Candidate> entry : sourceRuleSets.entrySet()) {
                Candidate candidate = entry.getValue();
                Optional<RuleFinding> finding = candidate.fit() ? thrownBy(candidate, thrown) : Optional.empty();
                if (finding.isPresent()) {
                    entry.setValue(candidate.refused(finding.get()));
                    this.inUse = build(sourceRuleSets.values());
                    LOG.info("Rule source \"{}\" taken out of use: {}", candidate.name(), finding.get().message());
                    return true;
                }
            }
            return false;
        }
    }

    private static Optional<RuleFinding> thrownBy(Candidate candidate, Throwable thrown) {
        Throwable root = rootOf(thrown);
        int depth = 0;
        for (Throwable cause = thrown; cause != null && depth < 32; cause = cause.getCause(), depth++) {
            for (StackTraceElement frame : cause.getStackTrace()) {
                for (Class<?> ruleClass : candidate.classes()) {
                    String className = ruleClass.getName();
                    if (frame.getClassName().equals(className) || frame.getClassName().startsWith(className + "$")) {
                        return Optional.of(new RuleFinding(Math.max(frame.getLineNumber(), 0),
                                "A rule threw when it fired: " + root));
                    }
                }
            }
        }
        return Optional.empty();
    }

    private static ImmutableList<RuleFinding> findingsOf(CompilationException refusal) {
        MutableList<RuleFinding> findings = Lists.mutable.empty();
        for (SourceCompiler.ClassSource refused : refusal.getErrorSources()) {
            for (String message : refusal.getErrorMessage(refused)) {
                Matcher line = COMPILER_LINE.matcher(message);
                findings.add(line.matches()
                        ? new RuleFinding(Integer.parseInt(line.group(1)), line.group(2).strip())
                        : new RuleFinding(0, message.strip()));
            }
        }
        if (findings.isEmpty()) {
            findings.add(new RuleFinding(0, inItsOwnWords(refusal)));
        }
        return findings.toImmutable();
    }

    private static String inItsOwnWords(Throwable refusal) {
        Throwable root = rootOf(refusal);
        String message = root.getMessage();
        if (message == null || message.isBlank()) {
            return root.toString();
        }
        if (message.contains(LITERAL_DATA_REFUSED)) {
            return "A condition written as text, as in @Where(\"…\"), is not taken. Write the condition as a "
                    + "boolean method and name it: @Where(methods = {@MethodPredicate(method = \"…\", args = {\"…\"})}).";
        }
        return message;
    }

    private static Throwable rootOf(Throwable thrown) {
        Throwable root = thrown;
        for (int depth = 0; root.getCause() != null && depth < 32; depth++) {
            root = root.getCause();
        }
        return root;
    }
}
