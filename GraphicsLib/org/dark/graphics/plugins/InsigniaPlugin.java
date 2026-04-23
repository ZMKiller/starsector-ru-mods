package org.dark.graphics.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.DeployedFleetMemberAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import java.util.ArrayList;
import java.util.List;
import org.dark.shaders.util.GraphicsLibSettings;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

@SuppressWarnings("UseSpecificCatch")
public class InsigniaPlugin extends BaseEveryFrameCombatPlugin {

    private CombatEngineAPI engine;

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }

    @Override
    public void renderInUICoords(ViewportAPI viewport) {
        if ((engine == null) || !GraphicsLibSettings.enableInsignias()) {
            return;
        }

        if (Global.getCombatEngine().isUIShowingHUD()) {
            for (ShipAPI ship : engine.getShips()) {
                if (!ship.isAlive() || ship.isFighter() || ship.isDrone() || (engine.getPlayerShip() == ship)) {
                    continue;
                }

                CombatFleetManagerAPI manager = engine.getFleetManager(ship.getOriginalOwner());
                if (manager == null) {
                    continue;
                }

                DeployedFleetMemberAPI deployedMember = manager.getDeployedFleetMember(ship);
                if (deployedMember == null) {
                    continue;
                }

                FleetMemberAPI member = deployedMember.getMember();
                if (member == null) {
                    continue;
                }

                CaptainTier tier = null;
                PersonAPI captain = member.getCaptain();

                boolean flagship = member.isFlagship();
                if (ship.getOriginalOwner() == 0) {
                    flagship = false;
                }

                if (captain == null) {
                    if (flagship) {
                        tier = CaptainTier.TIER_1;
                    }
                } else {
                    if (flagship) {
                        int level = captain.getStats().getLevel();
                        if (level < 3) {
                            tier = CaptainTier.TIER_1;
                        } else if (level < 5) {
                            tier = CaptainTier.TIER_2;
                        } else if (level < 7) {
                            tier = CaptainTier.TIER_3;
                        } else {
                            tier = CaptainTier.TIER_4;
                        }
                    } else {
                        if (!captain.isDefault()) {
                            int level = captain.getStats().getLevel();
                            if (level < 3) {
                                tier = CaptainTier.TIER_1_OFFICER;
                            } else if (level < 5) {
                                tier = CaptainTier.TIER_2_OFFICER;
                            } else if (level < 7) {
                                tier = CaptainTier.TIER_3_OFFICER;
                            } else {
                                tier = CaptainTier.TIER_4_OFFICER;
                            }
                        }
                    }
                }

                if (tier != null) {
                    List<Vector2f> corners = new ArrayList<>(4);
                    corners.add(new Vector2f(ship.getSpriteAPI().getWidth() * -0.5f, ship.getSpriteAPI().getHeight() * 0.5f));
                    corners.add(new Vector2f(ship.getSpriteAPI().getWidth() * 0.5f, ship.getSpriteAPI().getHeight() * 0.5f));
                    corners.add(new Vector2f(ship.getSpriteAPI().getWidth() * 0.5f, ship.getSpriteAPI().getHeight() * -0.5f));
                    corners.add(new Vector2f(ship.getSpriteAPI().getWidth() * -0.5f, ship.getSpriteAPI().getHeight() * -0.5f));
                    corners = VectorUtils.rotate(corners, ship.getFacing() - 90f);
                    for (Vector2f corner : corners) {
                        Vector2f.add(ship.getLocation(), corner, corner);
                        corner.x = viewport.convertWorldXtoScreenX(corner.x);
                        corner.y = viewport.convertWorldYtoScreenY(corner.y);
                    }

                    Float x = null;
                    Float y = null;
                    for (Vector2f corner : corners) {
                        if (x == null) {
                            x = corner.x;
                        } else {
                            x = Math.min(x, corner.x);
                        }
                        if (y == null) {
                            y = corner.y;
                        } else {
                            y = Math.max(y, corner.y);
                        }
                    }

                    float border = 18f;
                    float radius = ship.getCollisionRadius() / viewport.getViewMult();
                    Vector2f shipLoc = new Vector2f(viewport.convertWorldXtoScreenX(ship.getLocation().x),
                            viewport.convertWorldYtoScreenY(ship.getLocation().y));
                    Vector2f loc = new Vector2f(x, y);
                    Vector2f alternate = MathUtils.getPointOnCircumference(shipLoc, radius + border,
                            VectorUtils.getAngleStrict(shipLoc, loc));
                    x = alternate.x;
                    y = alternate.y;

                    CaptainTier.insignias.renderRegionAtCenter(x + tier.offsetX, y + tier.offsetY, tier.uvLeft,
                            tier.uvBottom,
                            (tier.uvRight - tier.uvLeft),
                            (tier.uvTop - tier.uvBottom));
                }
            }
        }
    }

    private static enum CaptainTier {

        TIER_1(3f / 3f, 2f / 3f, 0f / 3f, 1f / 3f, 26f, -26f),
        TIER_2(3f / 3f, 2f / 3f, 1f / 3f, 2f / 3f, 0f, -26f),
        TIER_3(3f / 3f, 2f / 3f, 2f / 3f, 3f / 3f, -26f, -26f),
        TIER_4(2f / 3f, 1f / 3f, 0f / 3f, 1f / 3f, 26f, 0f),
        TIER_1_OFFICER(2f / 3f, 1f / 3f, 1f / 3f, 2f / 3f, 0f, 0f),
        TIER_2_OFFICER(2f / 3f, 1f / 3f, 2f / 3f, 3f / 3f, -26f, 0f),
        TIER_3_OFFICER(1f / 3f, 0f / 3f, 0f / 3f, 1f / 3f, 26f, 26f),
        TIER_4_OFFICER(1f / 3f, 0f / 3f, 1f / 3f, 2f / 3f, 0f, 26f);

        static final SpriteAPI insignias = Global.getSettings().getSprite("icons", "glib_insignia");

        final float uvTop;
        final float uvBottom;
        final float uvLeft;
        final float uvRight;
        final float offsetX;
        final float offsetY;

        private CaptainTier(float uvTop, float uvBottom, float uvLeft, float uvRight, float offsetX, float offsetY) {
            this.uvTop = uvTop;
            this.uvBottom = uvBottom;
            this.uvLeft = uvLeft;
            this.uvRight = uvRight;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }
    }
}
