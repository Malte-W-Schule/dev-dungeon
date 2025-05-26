package item.effects;

import core.Entity;
import core.components.PlayerComponent;
import core.components.VelocityComponent;
import core.utils.components.MissingComponentException;
import systems.EventScheduler;

/**
 * Provides a mechanism to apply a temporary speed increase effect to an entity within the game.
 * Utilizing the EffectScheduler, this effect increases the entity's speed for a designated duration
 * before reverting it back to its original state. The implementation relies on scheduling both the
 * application of the speed increase and its subsequent reversal.
 */
public class SpeedEffect {
  private static final EventScheduler EVENT_SCHEDULER = EventScheduler.getInstance();
  private final float speedIncrease;
  private final int duration;

  /**
   * Initializes a new instance of the SpeedEffect with a specified increase in speed and duration.
   *
   * @param speedIncrease The amount to increase the entity's speed by.
   * @param duration The duration, in seconds, for which the speed increase is applied.
   */
  public SpeedEffect(float speedIncrease, int duration) {
    this.speedIncrease = speedIncrease;
    this.duration = duration;
  }

  /**
   * Applies a temporary speed increase to the target entity, then reverts its speed to normal after
   * the specified duration. The increase in speed is applied immediately, and its reversal will be
   * scheduled to occur after the duration expires.
   *
   * <p>TODO: Implement the applySpeedEffect method to schedule the speed increase and its
   * reversion.
   *
   * @param target The entity to which the speed effect will be applied.
   */
  public void applySpeedEffect(Entity target) {
      if (target.fetch(PlayerComponent.class).isEmpty()) {
          throw new UnsupportedOperationException(
              "Move speed can only be applied to player entities.");
      }

      int speedInterval = (int) (1 / this.speedIncrease);
      int totalSpeedEvents = (int) (this.duration * this.speedIncrease);

      //get the velocitycomponent
      VelocityComponent velocityComponent = target
          .fetch(VelocityComponent.class)
          .orElseThrow( ()-> MissingComponentException.build(target,PlayerComponent.class));

      //incrase the speed by
      velocityComponent.xVelocity( (velocityComponent.xVelocity()*speedIncrease) );
      velocityComponent.yVelocity( (velocityComponent.yVelocity()*speedIncrease) );

      //allowes to have for example a speed 150% and -speed 50% affect lead to
      // 100 * 1.5 = 150 -> 150*0.50 ->75 with debuff
      //after speed effect ends then -> 75/1.50= 50 and after debuff ends 50/0.5 = 100
      // nach x sekunden effekt zurück nehmen
      EVENT_SCHEDULER.scheduleAction(() -> {
          velocityComponent.xVelocity(velocityComponent.xVelocity() / speedIncrease);
          velocityComponent.yVelocity(velocityComponent.yVelocity() / speedIncrease);
      }, (long)(duration * 1000));
  }
}
