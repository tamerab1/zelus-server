FROM eclipse-temurin:21-jre-alpine AS runtime

LABEL org.opencontainers.image.title="Zelus Game Server"

# Non-root user for the game process
RUN addgroup -S zelus && adduser -S -G zelus -h /server zelus

WORKDIR /server

# Game data layers (ordered least-to-most-frequently-changing)
COPY --chown=zelus:zelus cache/data/cache         cache/data/cache
COPY --chown=zelus:zelus cache/data/grandexchange  cache/data/grandexchange
COPY --chown=zelus:zelus cache/data/objects        cache/data/objects
COPY --chown=zelus:zelus cache/data/npcs           cache/data/npcs
COPY --chown=zelus:zelus data                      data
COPY --chown=zelus:zelus app/build/install/near-reality-server/lib  application/lib
COPY --chown=zelus:zelus app/build/install/near-reality-server/bin  application/bin
COPY --chown=zelus:zelus NRPlugin.jar                               application/lib/NRPlugin.jar

# Scripts modules — installDist has filename collisions so copy each with a unique prefix
COPY --chown=zelus:zelus scripts/npc/spawns/build/libs/spawns-0.1.0.jar              application/lib/scripts-npc-spawns-0.1.0.jar
COPY --chown=zelus:zelus scripts/npc/definitions/build/libs/definitions-0.1.0.jar    application/lib/scripts-npc-definitions-0.1.0.jar
COPY --chown=zelus:zelus scripts/npc/drops/build/libs/drops-0.1.0.jar                application/lib/scripts-npc-drops-0.1.0.jar
COPY --chown=zelus:zelus scripts/npc/actions/build/libs/actions-0.1.0.jar            application/lib/scripts-npc-actions-0.1.0.jar
COPY --chown=zelus:zelus scripts/npc/build/libs/npc-0.1.0.jar                        application/lib/scripts-npc-0.1.0.jar
COPY --chown=zelus:zelus scripts/item/definitions/build/libs/definitions-0.1.0.jar   application/lib/scripts-item-definitions-0.1.0.jar
COPY --chown=zelus:zelus scripts/item/equip/build/libs/equip-0.1.0.jar               application/lib/scripts-item-equip-0.1.0.jar
COPY --chown=zelus:zelus scripts/item/actions/build/libs/actions-0.1.0.jar           application/lib/scripts-item-actions-0.1.0.jar
COPY --chown=zelus:zelus scripts/item/build/libs/item-0.1.0.jar                      application/lib/scripts-item-0.1.0.jar
COPY --chown=zelus:zelus scripts/object/actions/build/libs/actions-0.1.0.jar         application/lib/scripts-object-actions-0.1.0.jar
COPY --chown=zelus:zelus scripts/object/build/libs/object-0.1.0.jar                  application/lib/scripts-object-0.1.0.jar
COPY --chown=zelus:zelus scripts/player/actions/build/libs/actions-0.1.0.jar         application/lib/scripts-player-actions-0.1.0.jar
COPY --chown=zelus:zelus scripts/player/build/libs/player-0.1.0.jar                  application/lib/scripts-player-0.1.0.jar
COPY --chown=zelus:zelus scripts/shops/build/libs/shops-0.1.0.jar                    application/lib/scripts-shops-0.1.0.jar
COPY --chown=zelus:zelus scripts/common/build/libs/common-0.1.0.jar                  application/lib/scripts-common-0.1.0.jar
COPY --chown=zelus:zelus scripts/ground-items/build/libs/ground-items-0.1.0.jar      application/lib/scripts-ground-items-0.1.0.jar
COPY --chown=zelus:zelus scripts/interfaces/build/libs/interfaces-0.1.0.jar          application/lib/scripts-interfaces-0.1.0.jar
COPY --chown=zelus:zelus scripts/interfaces/user/build/libs/user-0.1.0.jar           application/lib/scripts-interfaces-user-0.1.0.jar
COPY --chown=zelus:zelus worlds.yml                                  worlds.yml
COPY --chown=zelus:zelus app/logback-dev.xml                         app/logback-dev.xml
COPY --chown=zelus:zelus app/logback-live.xml                        app/logback-live.xml
COPY --chown=zelus:zelus app/logback-beta.xml                        app/logback-beta.xml

# Persist character saves via Docker volume (mapped externally)
VOLUME ["/server/data/characters"]

USER zelus

EXPOSE 43594

HEALTHCHECK --interval=60s --timeout=10s --start-period=60s --retries=3 \
    CMD nc -z localhost 43594 || exit 1

# JVM flags: G1GC for low-latency, server mode, pre-touch memory
CMD ["java", \
     "-server", \
     "-Xmx4G", "-Xms2G", \
     "-XX:+UseG1GC", \
     "-XX:MaxGCPauseMillis=50", \
     "-XX:+ParallelRefProcEnabled", \
     "-XX:+AlwaysPreTouch", \
     "-XX:+DisableExplicitGC", \
     "-Dfile.encoding=UTF-8", \
     "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED", \
     "--add-opens=java.base/java.nio=ALL-UNNAMED", \
     "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED", \
     "--add-opens=java.base/java.lang=ALL-UNNAMED", \
     "--add-opens=java.base/java.io=ALL-UNNAMED", \
     "--add-opens=java.base/java.util=ALL-UNNAMED", \
     "--add-opens=java.base/java.util.concurrent=ALL-UNNAMED", \
     "--add-opens=java.base/java.time=ALL-UNNAMED", \
     "-classpath", "application/lib/*", \
     "com.zenyte.Main"]
