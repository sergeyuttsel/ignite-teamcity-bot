package org.apache.ignite.ci.web.rest;

import com.google.common.base.Strings;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.apache.ignite.Ignite;
import org.apache.ignite.ci.HelperConfig;
import org.apache.ignite.ci.IgnitePersistentTeamcity;
import org.apache.ignite.ci.IgniteTeamcityHelper;
import org.apache.ignite.ci.analysis.FullChainRunCtx;
import org.apache.ignite.ci.analysis.RunStat;
import org.apache.ignite.ci.conf.BranchTracked;
import org.apache.ignite.ci.conf.ChainAtServerTracked;
import org.apache.ignite.ci.web.BackgroundUpdater;
import org.apache.ignite.ci.web.CtxListener;
import org.apache.ignite.ci.web.rest.model.current.ChainAtServerCurrentStatus;
import org.apache.ignite.ci.web.rest.model.current.TestFailuresSummary;
import org.apache.ignite.internal.util.typedef.T3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.ignite.ci.runners.PrintChainResults.loadChainContext;

@Path(GetCurrTestFailures.CURRENT)
@Produces(MediaType.APPLICATION_JSON)
public class GetCurrTestFailures {
    public static final String CURRENT = "current";
    @Context
    private ServletContext context;

    @GET
    @Path("failures")
    public TestFailuresSummary getTestFails(@Nullable @QueryParam("branch") String branchOrNull) {
        final String key = Strings.nullToEmpty(branchOrNull);
        final BackgroundUpdater updater = (BackgroundUpdater)context.getAttribute(CtxListener.UPDATER);
        return updater.get(CURRENT + "TestFailuresSummary", key, this::getTestFailsNoCache);
    }

    @GET
    @Path("failuresNoCache")
    @NotNull public TestFailuresSummary getTestFailsNoCache(@Nullable @QueryParam("branch") String key) {
        final Ignite ignite = (Ignite)context.getAttribute(CtxListener.IGNITE);

        final TestFailuresSummary res = new TestFailuresSummary();
        final String branch = isNullOrEmpty(key) ? "master" : key;
        final BranchTracked tracked = HelperConfig.getTrackedBranches().getBranchMandatory(branch);

        for (ChainAtServerTracked chainTracked : tracked.chains) {
            try (IgnitePersistentTeamcity teamcity = new IgnitePersistentTeamcity(ignite, chainTracked.serverId)) {
                Optional<FullChainRunCtx> pubCtx = loadChainContext(teamcity,
                    chainTracked.getSuiteIdMandatory(),
                    chainTracked.getBranchForRestMandatory(),
                    true);

                final ChainAtServerCurrentStatus chainStatus = new ChainAtServerCurrentStatus();
                chainStatus.serverName = teamcity.serverId();

                final Map<String, RunStat> map = teamcity.runTestAnalysis();

                pubCtx.ifPresent(ctx -> chainStatus.initFromContext(teamcity, ctx, map));

                res.servers.add(chainStatus);
            }
        }
        return res;
    }


    @GET
    @Path("pr")
    public TestFailuresSummary getPrFailures(
        @Nullable @QueryParam("serverId") String serverId,
        @Nonnull @QueryParam("suiteId") String suiteId,
        @Nonnull @QueryParam("branchForTc") String branchForTc) {

        final BackgroundUpdater updater = (BackgroundUpdater)context.getAttribute(CtxListener.UPDATER);
        final T3<String, String, String> key = new T3<>(serverId, suiteId, branchForTc);

        return updater.get(CURRENT + "PrFailures", key,
            (key1) -> getPrFailuresNoCache(key1.get1(), key1.get2(), key1.get3()));
    }

    @GET
    @Path("prNoCache")
    @NotNull public TestFailuresSummary getPrFailuresNoCache(
        @Nullable @QueryParam("serverId") String serverId,
        @Nonnull @QueryParam("suiteId") String suiteId,
        @Nonnull @QueryParam("branchForTc") String branchForTc) {
        final Ignite ignite = (Ignite)context.getAttribute(CtxListener.IGNITE);

        final TestFailuresSummary res = new TestFailuresSummary();
        //using here non persistent TC allows to skip update statistic
        try (IgniteTeamcityHelper teamcity = new IgniteTeamcityHelper(serverId)) {
            Optional<FullChainRunCtx> pubCtx = loadChainContext(teamcity,
                suiteId, branchForTc,
                true);

            final ChainAtServerCurrentStatus chainStatus = new ChainAtServerCurrentStatus();
            chainStatus.serverName = teamcity.serverId();

            IgnitePersistentTeamcity teamcityP = new IgnitePersistentTeamcity(ignite, teamcity);
            final Map<String, RunStat> map = teamcityP.runTestAnalysis();

            pubCtx.ifPresent(ctx -> chainStatus.initFromContext(teamcity, ctx, map));

            res.servers.add(chainStatus);
        }
        return res;
    }

}
