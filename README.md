## Background
Various players have reported that the local difficulty is incorrectly calculated when playing on a multiplayer server. This is currently tracked on Mojang's Bug tracker as [MC-230732](https://bugs.mojang.com/browse/MC-230732) and [MC-197433](https://bugs.mojang.com/browse/MC-197433). This issue does not occur when playing on a singleplayer world.

## Investigation
The first thing that that was looked was the calculation used for the local difficulty. The method for this is located at `net.minecraft.world.LocalDifficulty.setLocalDifficulty`.
```java
private float setLocalDifficulty(Difficulty difficulty, long timeOfDay, long inhabitedTime, float moonSize) {
    if (difficulty == Difficulty.PEACEFUL) {
        return 0.0F;
    } else {
        boolean bl = difficulty == Difficulty.HARD;
        float f = 0.75F;
        float g = MathHelper.clamp(((float)timeOfDay + -72000.0F) / 1440000.0F, 0.0F, 1.0F) * 0.25F;
        f += g;
        float h = 0.0F;
        h += MathHelper.clamp((float)inhabitedTime / 3600000.0F, 0.0F, 1.0F) * (bl ? 1.0F : 0.75F);
        h += MathHelper.clamp(moonSize * 0.25F, 0.0F, g);
        if (difficulty == Difficulty.EASY) {
            h *= 0.5F;
        }

        f += h;
        return (float)difficulty.getId() * f;
    }
}
```

To help with debugging, the code was translated to the following TypeScript:
```typescript
enum Difficulty {
  PEACEFUL = 0,
  EASY = 1,
  NORMAL = 2,
  HARD = 3,
}

function clamp(value: number, min: number, max: number) {
  return Math.max(min, Math.min(value, max));
}

function setLocalDifficulty(
  // world difficulty
  difficulty: Difficulty,
  // the current "time of day" in the world
  worldTime: number,
  // how long the chunk has had at least one player in it
  inhabitedTime: number,
  // moon phase, possible values: 1.00, 0.75, 0.50, 0.25, 0.00
  moonSize: number,
) {
  if (difficulty === Difficulty.PEACEFUL) {
    return 0;
  } else {
    const isHardDifficulty: boolean = difficulty === Difficulty.HARD;

    let localDifficulty = 0.75;

    // 72000 = 3 mc days, 1440000 = 60 mc days
    // maximum this can contribute is achieved after 60 mc days, and is 0.25
    const modifier1 = clamp((worldTime + -72000) / 1440000, 0, 1) * 0.25;

    // max modifier1 = 0.25
    localDifficulty += modifier1;

    let modifier2 = 0;

    // 3600000 = 150 mcdays
    // maximum this can contribute is achieved after 150 mcdays, and is 1 if it's hard difficulty, otherwise 0.75
    modifier2 += clamp(inhabitedTime / 3600000, 0, 1) * (isHardDifficulty ? 1 : 0.75);

    // maximum this can contribute is 0.25
    modifier2 += clamp(moonSize * 0.25, 0, modifier1);

    // if it's easy difficulty, then this modifier is halved
    if (difficulty === Difficulty.EASY) {
      modifier2 *= 0.5;
    }

    // max modifier2 = 1.25
    localDifficulty += modifier2;

    // max returned value = 6.75
    return difficulty * localDifficulty;
  }
}
```

After testing the method with various input parameters, nothing was found to be inherently wrong with it, so the next step was to look at the values that were being passed into the calculation. A custom fabric mod was written to add a command that would print the values that were being passed into the method, and the values that were being returned. The mod can be found [here](https://).

Testing with a frequently occupied chunk, a discrepancy can be observed between the local diffculty displaced in the F3 debug menu and the output of the command
![image](https://t89.s3-us-west-1.amazonaws.com/2023/04/8wbyEcHq/javaw.png)

However, testing with a newly generated chunk, the values are the same.
![image](https://t89.s3-us-west-1.amazonaws.com/2023/04/Z83TSiYX/javaw.png)

This led to the suspicion that on the client, the inhabited time used in the calculation was 0.

To confirm this, the protocol was inspected. Specifically, the class at `net.minecraft.world.chunk.ProtoChunk` was looked at. It can be seen that the inhabited time is not sent over the network, and defaults to 0.
![image](https://t89.s3-us-west-1.amazonaws.com/2023/04/9q8HyCb2/idea64.png)

## Conclusion
The local difficulty is calculated incorrectly on the client when playing on a multiplayer server. This is due to the client not receiving the inhabited time of the chunk, and therefore using 0 as the value. This issue is only present while playing on multiplayer servers, since on single player, there is no network communication, and the inhabited time is calculated retrieved from the directly from the chunk data. **This is only a visual issue, and does not affect gameplay, as the local difficulty is still correct on the server side.**