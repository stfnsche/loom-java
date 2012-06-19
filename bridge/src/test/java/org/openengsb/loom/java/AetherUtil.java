package org.openengsb.loom.java;

import java.io.File;

import org.apache.maven.repository.internal.MavenRepositorySystemSession;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.collection.CollectRequest;
import org.sonatype.aether.collection.CollectResult;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyRequest;
import org.sonatype.aether.util.artifact.DefaultArtifact;

public class AetherUtil {

    private static RepositorySystem newRepositorySystem() throws Exception {
        return new DefaultPlexusContainer().lookup(RepositorySystem.class);
    }

    private static RepositorySystemSession newSession(RepositorySystem system) {
        MavenRepositorySystemSession session = new MavenRepositorySystemSession();
        String homedir = System.getProperty("user.home");
        LocalRepository localRepo = new LocalRepository(new File(homedir, ".m2/repository"));
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(localRepo));
        return session;
    }

    public static File downloadArtifact(String openengsbVersion) throws Exception {
        RepositorySystem repoSystem = newRepositorySystem();
        RepositorySystemSession session = newSession(repoSystem);
        DefaultArtifact artifact =
            new DefaultArtifact("org.openengsb.framework", "openengsb-framework", "zip", openengsbVersion);
        Dependency dependency =
            new Dependency(artifact, "test");
        RemoteRepository central = new RemoteRepository("central", "default", "http://repo1.maven.org/maven2/");
        RemoteRepository sonatypeSnapshots =
            new RemoteRepository("sonatype-snapshots", "default",
                "https://oss.sonatype.org/content/repositories/snapshots/");

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(dependency);
        collectRequest.addRepository(sonatypeSnapshots);
        collectRequest.addRepository(central);

        System.out.println("collecting");
        CollectResult collectDependencies = repoSystem.collectDependencies(session, collectRequest);
        DependencyNode node = collectDependencies.getRoot();
        DependencyRequest dependencyRequest = new DependencyRequest(node, null);
        repoSystem.resolveDependencies(session, dependencyRequest);

        return node.getDependency().getArtifact().getFile();
    }

}