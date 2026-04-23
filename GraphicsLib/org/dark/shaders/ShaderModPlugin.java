package org.dark.shaders;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Level;
import org.dark.graphics.util.ShipColors;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.light.LightData;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.post.PostProcessShader;
import org.dark.shaders.util.GraphicsLibSettings;
import org.dark.shaders.util.ShaderLib;
import org.dark.shaders.util.TextureData;
import org.json.JSONException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

/**
 * This is the base mod plugin for ShaderLib, which initializes its shaders.
 * <p>
 * @author DarkRevenant
 */
public final class ShaderModPlugin extends BaseModPlugin {

    public static final Map<Integer, String> ASTEROID_MAP = new HashMap<>(16);

    public static boolean templarsExists = false;

    public static void refresh() {
        try {
            Display.processMessages();
        } catch (Throwable t) {
        }
    }

    @Override
    public void onGameLoad(boolean newGame) {
        SectorAPI sector = Global.getSector();
        if ((sector != null) && !sector.getListenerManager().hasListenerOfClass(ShaderTextureUnloader.class)) {
            sector.getListenerManager().addListener(new ShaderTextureUnloader(), true);
        }
    }

    @Override
    @SuppressWarnings("UseSpecificCatch")
    public void onApplicationLoad() throws IOException, JSONException {
        Global.getLogger(ShaderModPlugin.class).setLevel(Level.INFO);

        templarsExists = Global.getSettings().getModManager().isModEnabled("Templars");

        ASTEROID_MAP.put(Global.getSettings().getSprite("graphics/asteroids/asteroid1.png").getTextureId(), "asteroid1");
        ASTEROID_MAP.put(Global.getSettings().getSprite("graphics/asteroids/asteroid2.png").getTextureId(), "asteroid2");
        ASTEROID_MAP.put(Global.getSettings().getSprite("graphics/asteroids/asteroid3.png").getTextureId(), "asteroid3");
        ASTEROID_MAP.put(Global.getSettings().getSprite("graphics/asteroids/asteroid4.png").getTextureId(), "asteroid4");
        ASTEROID_MAP.put(Global.getSettings().getSprite("graphics/asteroids/ring_asteroid00.png").getTextureId(), "ring_asteroid00");
        ASTEROID_MAP.put(Global.getSettings().getSprite("graphics/asteroids/ring_asteroid01.png").getTextureId(), "ring_asteroid01");
        ASTEROID_MAP.put(Global.getSettings().getSprite("graphics/asteroids/ring_asteroid02.png").getTextureId(), "ring_asteroid02");
        ASTEROID_MAP.put(Global.getSettings().getSprite("graphics/asteroids/ring_asteroid03.png").getTextureId(), "ring_asteroid03");
//        ASTEROID_MAP.put(Global.getSettings().getSprite("graphics/asteroids/ring_asteroid04.png"), "ring_asteroid04");
//        ASTEROID_MAP.put(Global.getSettings().getSprite("graphics/asteroids/ring_asteroid05.png"), "ring_asteroid05");
//        ASTEROID_MAP.put(Global.getSettings().getSprite("graphics/asteroids/ring_asteroid06.png"), "ring_asteroid06");
//        ASTEROID_MAP.put(Global.getSettings().getSprite("graphics/asteroids/ring_asteroid07.png"), "ring_asteroid07");
//        ASTEROID_MAP.put(Global.getSettings().getSprite("graphics/asteroids/ring_asteroid08.png"), "ring_asteroid08");
//        ASTEROID_MAP.put(Global.getSettings().getSprite("graphics/asteroids/ring_asteroid09.png"), "ring_asteroid09");
//        ASTEROID_MAP.put(Global.getSettings().getSprite("graphics/asteroids/ring_asteroid10.png"), "ring_asteroid10");
//        ASTEROID_MAP.put(Global.getSettings().getSprite("graphics/asteroids/ring_asteroid11.png"), "ring_asteroid11");

        ShaderLib.init();
        ShipColors.init();

        if (ShaderLib.areShadersAllowed()) {
            if ((Global.getSettings().getAASamples() > 1) && !ShaderLib.isAACompatMode()) {
                if (!Global.getSettings().getModManager().isModEnabled("lunalib")) {
                    throw new RuntimeException("""
GraphicsLib shaders are not fully compatible with antialiasing! You have three options...

A) Set Antialiasing to "Off" in the launcher options.

B) Use partial antialiasing by setting "aaCompatMode" to "true" in GRAPHICS_OPTIONS.ini.

C) Disable shaders by setting "enableShaders" to "false" in GRAPHICS_OPTIONS.ini.
                                           """);
                }
            }
        }

        refresh();

        //ShaderLib.addShaderAPI(new LensShader());
        //ShaderLib.addShaderAPI(new InvertShader());
        ShaderLib.addShaderAPI(new PostProcessShader());

        LightData.readLightDataCSVNoOverwrite("data/lights/core_light_data.csv");
        TextureData.readTextureDataCSVNoOverwrite("data/lights/core_texture_data.csv");
        ShaderLib.addShaderAPI(new LightShader());
        ShaderLib.addShaderAPI(new DistortionShader());

        if (GraphicsLibSettings.enableDistortion()) {
            GraphicsLibSettings.loadWave();
        }

        if (GraphicsLibSettings.useSmallRipple()) {
            GraphicsLibSettings.loadSmallRipple();
        } else if (GraphicsLibSettings.useLargeRipple()) {
            GraphicsLibSettings.loadLargeRipple();
        }

        if (TextureData.isLoadMaterial() || TextureData.isLoadNormal() || TextureData.isLoadSurface()) {
            GraphicsLibSettings.loadThreat();
        }

        /*
         * EXPERIMENTAL
         */
        if (false) {
            try {
                final long startTime = System.nanoTime();

                final Class<?> fileClass = Class.forName("java.io.File", false, Class.class.getClassLoader());
                final Class<?> imageIoClass = Class.forName("javax.imageio.ImageIO", true, Class.class.getClassLoader());

                final MethodHandle filePathConstructor = MethodHandles.lookup().findConstructor(fileClass, MethodType.methodType(void.class, String.class, String.class));
                final MethodHandle imageioWrite = MethodHandles.lookup().findStatic(imageIoClass, "write", MethodType.methodType(boolean.class, RenderedImage.class, String.class, fileClass));

                final int prevBoundTex = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
                final SpriteAPI sprite = Global.getSettings().getSprite("graphics/ships/anubis/anubis_base.png");
                sprite.bindTexture();
                final int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
                final int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
                final int size = width * height * 4;
                final int trueWidth = Math.round(width * sprite.getTextureWidth());
                final int trueHeight = Math.round(height * sprite.getTextureHeight());
                final int trueSize = trueWidth * trueHeight * 4;

                ByteBuffer texBuffer = ByteBuffer.allocateDirect(size);
                GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, texBuffer);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, prevBoundTex);

                BufferedImage newImage = new BufferedImage(trueWidth, trueHeight, BufferedImage.TYPE_4BYTE_ABGR);
                WritableRaster newImageData = newImage.getRaster();
                int yDiff = height - trueHeight;
                int pixel[] = new int[4];
                byte heightData[] = new byte[trueSize];
                byte normalData[] = new byte[trueSize];
                double heightMap[][] = new double[trueWidth][trueHeight];
                double avgLuminosity = 0.0;
                double pixelWeight = 0.0;
                for (int y = (height - 1); y >= 0; y--) {
                    for (int x = 0; x < width; x++) {
                        pixel[0] = texBuffer.get();
                        pixel[1] = texBuffer.get();
                        pixel[2] = texBuffer.get();
                        pixel[3] = texBuffer.get();
                        if (x >= trueWidth) {
                            continue;
                        }
                        if (y < yDiff) {
                            continue;
                        }
                        int idx = (x + ((y - yDiff) * trueWidth)) * 4;
                        heightData[idx] = (byte) pixel[3]; // Alpha
                        normalData[idx] = (byte) pixel[3]; // Alpha
                        double alpha = (pixel[3] < 0 ? pixel[3] + 256 : pixel[3]) / 255.0;
                        double red = ((pixel[0] < 0 ? pixel[0] + 256 : pixel[0]) / 255.0) * alpha;
                        double green = ((pixel[1] < 0 ? pixel[1] + 256 : pixel[1]) / 255.0) * alpha;
                        double blue = ((pixel[2] < 0 ? pixel[2] + 256 : pixel[2]) / 255.0) * alpha;
                        heightMap[x][y - yDiff] = Math.max(0.0, Math.min(1.0, Math.sqrt((0.299 * red * red) + (0.587 * green * green) + (0.114 * blue * blue))));
                        avgLuminosity += heightMap[x][y - yDiff];
                        pixelWeight += alpha;
                        newImageData.setPixel(x, y - yDiff, pixel);
                    }
                }

                if (pixelWeight > 1E-6) {
                    avgLuminosity /= pixelWeight;
                }
                double heightExponent = 1.0;
                if (avgLuminosity > 1E-6) {
                    heightExponent = Math.log(0.5) / Math.log(avgLuminosity);
                }
                System.out.println("avgLuminosity = " + avgLuminosity);
                System.out.println("heightExponent = " + heightExponent);
                avgLuminosity = 0.0;
                for (int y = (trueHeight - 1); y >= 0; y--) {
                    for (int x = 0; x < trueWidth; x++) {
                        heightMap[x][y] = Math.pow(heightMap[x][y], heightExponent);
                        avgLuminosity += heightMap[x][y];
                    }
                }

                if (pixelWeight > 1E-6) {
                    avgLuminosity /= pixelWeight;
                }
                double rmsContrast = 0.0;
                for (int y = (trueHeight - 1); y >= 0; y--) {
                    for (int x = 0; x < trueWidth; x++) {
                        int idx = (x + (y * trueWidth)) * 4;
                        double alpha = (normalData[idx] < 0 ? normalData[idx] + 256 : normalData[idx]) / 255.0;
                        double deviation = heightMap[x][y] - avgLuminosity;
                        rmsContrast += deviation * deviation * alpha;
                    }
                }
                System.out.println("avgLuminosity = " + avgLuminosity);
                double heightMult = 1.0;
                if (pixelWeight > 1E-6) {
                    rmsContrast = Math.sqrt(rmsContrast / pixelWeight);
                    heightMult = (Math.sqrt(3.0) / 6.0) / rmsContrast;
                }
                System.out.println("rmsContrast = " + rmsContrast);
                double heightBias = 0.5 - (avgLuminosity * heightMult);
                System.out.println("heightMult = " + heightMult);
                System.out.println("heightBias = " + heightBias);
                for (int y = (trueHeight - 1); y >= 0; y--) {
                    for (int x = 0; x < trueWidth; x++) {
                        double xRatio = (double) ((trueWidth - 1) - x) / (double) (trueWidth - 1);
                        double yRatio = (double) ((trueHeight - 1) - y) / (double) (trueHeight - 1);
                        double crossFade = Math.sqrt(1.0 - (1.75 * Math.abs(xRatio - 0.5))) * Math.sqrt(1.0 - Math.abs(yRatio - 0.5));
                        heightMap[x][y] = ((((heightMap[x][y] * heightMult) + heightBias) * crossFade) + (crossFade - 0.25)) / 1.75;
                    }
                }

                double blurHeightMap[][] = new double[trueWidth][trueHeight];
                double[] kernel = new double[]{1.0, 1.0 / 4.0, 1.0 / 9.0, 1.0 / 16.0, 1.0 / 25.0, 1.0 / 36.0, 1.0 / 49.0, 1.0 / 64.0,
                    1.0 / 81.0, 1.0 / 100.0, 1.0 / 121.0, 1.0 / 144.0, 1.0 / 169.0, 1.0 / 196.0, 1.0 / 225.0, 1.0 / 256.0};
                int kernelSemiSize = kernel.length;
                double kernelSum = kernel[0];
                for (int i = 1; i < kernelSemiSize; i++) {
                    kernelSum += kernel[i] * 2.0;
                }
                for (int i = 0; i < kernelSemiSize; i++) {
                    kernel[i] /= kernelSum;
                }
                for (int x = 0; x < trueWidth; x++) {
                    for (int y = (trueHeight - 1); y >= 0; y--) {
                        blurHeightMap[x][y] = heightMap[x][y] * kernel[0];
                        for (int i = 1; i < kernelSemiSize; i++) {
                            final int x1 = Math.max(0, x - i);
                            if (x1 >= 0) {
                                blurHeightMap[x][y] += heightMap[x1][y] * kernel[i];
                            }
                            final int x2 = Math.min(trueWidth - 1, x + i);
                            if (x2 < trueWidth) {
                                blurHeightMap[x][y] += heightMap[x2][y] * kernel[i];
                            }
                        }
                    }
                }
                for (int x = 0; x < trueWidth; x++) {
                    for (int y = (trueHeight - 1); y >= 0; y--) {
                        heightMap[x][y] = blurHeightMap[x][y] * kernel[0];
                        for (int i = 1; i < kernelSemiSize; i++) {
                            final int y1 = Math.max(0, y - i);
                            if (y1 >= 0) {
                                heightMap[x][y] += blurHeightMap[x][y1] * kernel[i];
                            }
                            final int y2 = Math.min(trueHeight - 1, y + i);
                            if (y2 < trueHeight) {
                                heightMap[x][y] += blurHeightMap[x][y2] * kernel[i];
                            }
                        }
                    }
                }

                img2normalwithheight(heightMap, heightData, normalData);

                BufferedImage heightImage = new BufferedImage(trueWidth, trueHeight, BufferedImage.TYPE_4BYTE_ABGR);
                DataBufferByte heightImageBuffer = (DataBufferByte) heightImage.getRaster().getDataBuffer();
                System.arraycopy(heightData, 0, heightImageBuffer.getData(), 0, trueSize);

                BufferedImage normalImage = new BufferedImage(trueWidth, trueHeight, BufferedImage.TYPE_4BYTE_ABGR);
                DataBufferByte normalImageBuffer = (DataBufferByte) normalImage.getRaster().getDataBuffer();
                System.arraycopy(normalData, 0, normalImageBuffer.getData(), 0, trueSize);

                final String graphicsLibPath = Global.getSettings().getModManager().getModSpec("shaderLib").getPath();
                final Object testWriteFile = filePathConstructor.invoke(graphicsLibPath, "cache/testWrite.png");
                final Object testHeightFile = filePathConstructor.invoke(graphicsLibPath, "cache/testHeight.png");
                final Object testNormalFile = filePathConstructor.invoke(graphicsLibPath, "cache/testNormal.png");

                imageioWrite.invoke((RenderedImage) newImage, "png", testWriteFile);
                imageioWrite.invoke((RenderedImage) heightImage, "png", testHeightFile);
                imageioWrite.invoke((RenderedImage) normalImage, "png", testNormalFile);

                Global.getSettings().forceMipmapsFor("cache/testNormal.png", true);
                Global.getSettings().loadTexture("cache/testNormal.png");

                final long endTime = System.nanoTime();
                System.out.println("Took " + (endTime - startTime) / 1000000.0 + " milliseconds");
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | IOException e) {
                Global.getLogger(ShaderModPlugin.class).log(Level.ERROR, e.getMessage());
            } catch (Throwable e) {
                Global.getLogger(ShaderModPlugin.class).log(Level.ERROR, e.getMessage());
            }
        }

        if (true) {
            TextureData.autoGenMissingNormalMaps();
        }
    }

    private static void img2normalwithheight(double heightMap[][], byte heightData[], byte normalData[]) {
        final int width = heightMap.length;
        final int height = heightMap[0].length;
        final double dz = 0.133333;
        Vector3f normal = new Vector3f();
        for (int y = (height - 1); y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                final int idx = (x + (y * width)) * 4;
                heightData[idx + 3] = (byte) Math.max(0, Math.min(255, Math.round(heightMap[x][y] * 255.0))); // Red
                heightData[idx + 2] = (byte) Math.max(0, Math.min(255, Math.round(heightMap[x][y] * 255.0))); // Green
                heightData[idx + 1] = (byte) Math.max(0, Math.min(255, Math.round(heightMap[x][y] * 255.0))); // Blue

                final double tl = heightMap[Math.max(0, x - 1)][Math.max(0, y - 1)];
                final double l = heightMap[Math.max(0, x - 1)][y];
                final double bl = heightMap[Math.max(0, x - 1)][Math.min(height - 1, y + 1)];
                final double t = heightMap[x][Math.max(0, y - 1)];
                final double b = heightMap[x][Math.min(height - 1, y + 1)];
                final double tr = heightMap[Math.min(width - 1, x + 1)][Math.max(0, y - 1)];
                final double r = heightMap[Math.min(width - 1, x + 1)][y];
                final double br = heightMap[Math.min(width - 1, x + 1)][Math.min(height - 1, y + 1)];

                final double dx = tl + (l * 2.0) + bl - tr - (r * 2.0) - br;
                final double dy = tl + (t * 2.0) + tr - bl - (b * 2.0) - br;
                normal.set((float) dx, (float) dy, (float) dz);
                normal.normalise();

                normalData[idx + 3] = (byte) Math.max(0, Math.min(255, Math.round((0.5f + (0.5f * normal.x)) * 255f))); // Red/X
                normalData[idx + 2] = (byte) Math.max(0, Math.min(255, Math.round((0.5f + (0.5f * normal.y)) * 255f))); // Green/Y
                normalData[idx + 1] = (byte) Math.max(0, Math.min(255, Math.round((0.5f + (0.5f * normal.z)) * 255f))); // Blue/Z
            }
        }
    }

    private static class ShaderTextureUnloader implements FleetEventListener {

        @Override
        public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
            /* Pass */
        }

        @Override
        public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
            if (battle.isPlayerInvolved()) {
                /* Unload unneeded textures at the end of combat */
                CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
                if (playerFleet != null) {
                    List<FleetMemberAPI> members = playerFleet.getFleetData().getMembersListCopy();
                    TextureData.unloadAndPreloadTextures(members);
                }
            }
        }
    }
}
