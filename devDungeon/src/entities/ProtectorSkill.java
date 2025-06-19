package entities;

import contrib.components.AIComponent;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.MissingComponentException;

import java.io.IOException;
import java.util.Random;
import java.util.function.Consumer;

public class ProtectorSkill implements Consumer<Entity> {

    private static final float SPAWN_RADIUS = 3.0f;
    private static final Random RANDOM = new Random();

    @Override
    public void accept(Entity hero) {
        try {
            spawnProtector(hero);
        } catch (IOException e) {
            System.err.println("Failed to spawn protector: " + e.getMessage());
        }
    }

    public void spawnProtector(Entity hero) throws IOException {
        PositionComponent heroPos = hero.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(hero, PositionComponent.class));

        Point spawnPosition = getRandomPositionAroundHero(heroPos.position());

        Entity protector = MonsterType.PROTECTOR.buildMonster();

        PositionComponent protectorPos = protector.fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(protector, PositionComponent.class));

        protectorPos.position(spawnPosition);

        Game.add(protector);
    }

    private Point getRandomPositionAroundHero(Point heroPosition) {
        float angle = RANDOM.nextFloat() * 2 * (float) Math.PI;
        float distance = RANDOM.nextFloat() * SPAWN_RADIUS + 1.0f;

        float x = heroPosition.x + (float) Math.cos(angle) * distance;
        float y = heroPosition.y + (float) Math.sin(angle) * distance;

        return new Point(x, y);
    }

    public static Point positionOfNearestHostileEntity() {
        Entity hero = Game.hero().orElse(null);
        if (hero == null) return new Point(0, 0);

        PositionComponent heroPos = hero.fetch(PositionComponent.class).orElse(null);
        if (heroPos == null) return new Point(0, 0);

        Entity nearestEnemy = null;
        float minDistance = Float.MAX_VALUE;

        for (Entity entity : Game.entityStream().toList()) {
            if (entity == hero) continue;
            if (!entity.isPresent(AIComponent.class)) continue;

            PositionComponent entityPos = entity.fetch(PositionComponent.class).orElse(null);
            if (entityPos == null) continue;

            float distance = Point.calculateDistance(heroPos.position(), entityPos.position());
            if (distance < minDistance) {
                minDistance = distance;
                nearestEnemy = entity;
            }
        }

        if (nearestEnemy != null) {
            PositionComponent enemyPos = nearestEnemy.fetch(PositionComponent.class).orElse(null);
            if (enemyPos != null) {
                return enemyPos.position();
            }
        }

        return heroPos.position();
    }
}
