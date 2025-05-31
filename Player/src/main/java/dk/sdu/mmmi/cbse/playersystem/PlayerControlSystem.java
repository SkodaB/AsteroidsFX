package dk.sdu.mmmi.cbse.playersystem;

import dk.sdu.mmmi.cbse.common.bullet.Bullet;
import dk.sdu.mmmi.cbse.common.bullet.BulletSPI;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.GameKeys;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IEntityProcessingService;

import java.util.Collection;
import java.util.ServiceLoader;

import static java.util.stream.Collectors.toList;

public class PlayerControlSystem implements IEntityProcessingService {

    @Override
    public void process(GameData gameData, World world) {

        for (Entity player : world.getEntities(Player.class)) {

            // Rotate player to face mouse position
            float dx = gameData.getMouseX() - (float) player.getX();
            float dy = gameData.getMouseY() - (float) player.getY();
            float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
            player.setRotation(angle);

            double radians = Math.toRadians(player.getRotation());
            double forwardX = Math.cos(radians);
            double forwardY = Math.sin(radians);
            double strafeX = Math.cos(radians + Math.PI / 2);
            double strafeY = Math.sin(radians + Math.PI / 2);

            // W = forward
            if (gameData.getKeys().isDown(GameKeys.UP)) {
                player.setX(player.getX() + forwardX);
                player.setY(player.getY() + forwardY);
            }

            // S = backward
            if (gameData.getKeys().isDown(GameKeys.DOWN)) {
                player.setX(player.getX() - forwardX);
                player.setY(player.getY() - forwardY);
            }

            // A = strafe left
            if (gameData.getKeys().isDown(GameKeys.LEFT)) {
                player.setX(player.getX() + strafeX);
                player.setY(player.getY() + strafeY);
            }

            // D = strafe right
            if (gameData.getKeys().isDown(GameKeys.RIGHT)) {
                player.setX(player.getX() - strafeX);
                player.setY(player.getY() - strafeY);
            }

            // Shoot with SPACE
            if (gameData.getKeys().isDown(GameKeys.SPACE)) {
                getBulletSPIs().stream().findFirst().ifPresent(
                        spi -> world.addEntity(spi.createBullet(player, gameData))
                );
            }

            // Keep within screen
            if (player.getX() < 0) player.setX(1);
            if (player.getX() > gameData.getDisplayWidth()) player.setX(gameData.getDisplayWidth() - 1);
            if (player.getY() < 0) player.setY(1);
            if (player.getY() > gameData.getDisplayHeight()) player.setY(gameData.getDisplayHeight() - 1);
        }
    }

    private Collection<? extends BulletSPI> getBulletSPIs() {
        return ServiceLoader.load(BulletSPI.class).stream().map(ServiceLoader.Provider::get).collect(toList());
    }
}
