package quek.undergarden.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IRenderHandler;
import net.minecraftforge.client.SkyRenderHandler;
import quek.undergarden.UndergardenMod;

import javax.annotation.Nullable;
import java.util.Random;

public class OthersideSkyRender implements SkyRenderHandler {

    private static final ResourceLocation VORTEX_TEXTURE = new ResourceLocation(UndergardenMod.MODID,"textures/environment/otherside_vortex.png");

    private final VertexFormat skyVertexFormat = DefaultVertexFormats.POSITION;
    @Nullable
    private VertexBuffer starVBO, skyVBO, sky2VBO;


    public OthersideSkyRender() {
        this.generateSky();
        this.generateSky2();
        this.generateStars();
    }

    private void generateStars() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        if (this.starVBO != null) {
            this.starVBO.close();
        }

        this.starVBO = new VertexBuffer(this.skyVertexFormat);
        this.renderStars(bufferbuilder);
        bufferbuilder.finishDrawing();
        this.starVBO.upload(bufferbuilder);
    }


    private void renderStars(BufferBuilder bufferBuilderIn) {
        Random random = new Random(10842L);
        bufferBuilderIn.begin(7, DefaultVertexFormats.POSITION);

        for (int i = 0; i < 1500; ++i) {
            double d0 = random.nextFloat() * 2.0F - 1.0F;
            double d1 = random.nextFloat() * 2.0F - 1.0F;
            double d2 = random.nextFloat() * 2.0F - 1.0F;
            double d3 = 0.15F + random.nextFloat() * 0.1F;
            double d4 = d0 * d0 + d1 * d1 + d2 * d2;
            if (d4 < 1.0D && d4 > 0.01D) {
                d4 = 1.0D / Math.sqrt(d4);
                d0 = d0 * d4;
                d1 = d1 * d4;
                d2 = d2 * d4;
                double d5 = d0 * 200.0D;
                double d6 = d1 * 200.0D;
                double d7 = d2 * 200.0D;
                double d8 = Math.atan2(d0, d2);
                double d9 = Math.sin(d8);
                double d10 = Math.cos(d8);
                double d11 = Math.atan2(Math.sqrt(d0 * d0 + d2 * d2), d1);
                double d12 = Math.sin(d11);
                double d13 = Math.cos(d11);
                double d14 = random.nextDouble() * Math.PI * 2.0D;
                double d15 = Math.sin(d14);
                double d16 = Math.cos(d14);

                for (int j = 0; j < 4; ++j) {
                    double d18 = ((j & 2) - 1) * d3;
                    double d19 = ((j + 1 & 2) - 1) * d3;
                    double d21 = d18 * d16 - d19 * d15;
                    double d22 = d19 * d16 + d18 * d15;
                    double d23 = d21 * d12 + 0.0D * d13;
                    double d24 = 0.0D * d12 - d21 * d13;
                    double d25 = d24 * d9 - d22 * d10;
                    double d26 = d22 * d9 + d24 * d10;
                    bufferBuilderIn.pos(d5 + d25, d6 + d23, d7 + d26).endVertex();
                }
            }
        }

    }

    private void generateSky() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        if (this.skyVBO != null) {
            this.skyVBO.close();
        }

        this.skyVBO = new VertexBuffer(this.skyVertexFormat);
        this.renderSky(bufferbuilder, 16.0F, false);
        bufferbuilder.finishDrawing();
        this.skyVBO.upload(bufferbuilder);
    }

    private void renderSky(BufferBuilder bufferBuilderIn, float posY, boolean reverseX) {
        bufferBuilderIn.begin(7, DefaultVertexFormats.POSITION);

        for(int k = -384; k <= 384; k += 64) {
            for(int l = -384; l <= 384; l += 64) {
                float f = (float)k;
                float f1 = (float)(k + 64);
                if (reverseX) {
                    f1 = (float)k;
                    f = (float)(k + 64);
                }

                bufferBuilderIn.pos(f, posY, l).endVertex();
                bufferBuilderIn.pos(f1, posY, l).endVertex();
                bufferBuilderIn.pos(f1, posY, (l + 64)).endVertex();
                bufferBuilderIn.pos(f, posY, (l + 64)).endVertex();
            }
        }

    }

    private void generateSky2() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        if (this.sky2VBO != null) {
            this.sky2VBO.close();
        }

        this.sky2VBO = new VertexBuffer(this.skyVertexFormat);
        this.renderSky(bufferbuilder, -16.0F, true);
        bufferbuilder.finishDrawing();
        this.sky2VBO.upload(bufferbuilder);
    }

    @Override
    public void render(int ticks, float partialTicks, MatrixStack matrixStack, ClientWorld world, Minecraft mc) {
        TextureManager textureManager = mc.getTextureManager();
        RenderSystem.disableTexture();
        Vec3d vec3d = world.getSkyColor(mc.gameRenderer.getActiveRenderInfo().getBlockPos(), partialTicks);
        float f = (float)vec3d.x;
        float f1 = (float)vec3d.y;
        float f2 = (float)vec3d.z;
        FogRenderer.applyFog();
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.depthMask(false);
        RenderSystem.enableFog();
        RenderSystem.color3f(f, f1, f2);
        this.skyVBO.bindBuffer();
        this.skyVertexFormat.setupBufferState(0L);
        this.skyVBO.draw(matrixStack.getLast().getMatrix(), 7);
        VertexBuffer.unbindBuffer();
        this.skyVertexFormat.clearBufferState();
        RenderSystem.disableFog();
        RenderSystem.disableAlphaTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();


        RenderSystem.enableTexture();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        matrixStack.push();
        RenderSystem.color4f(0.5F, 0.5F, 0.5F, 1.0F);
        matrixStack.rotate(Vector3f.YP.rotationDegrees(-180.0F));
        matrixStack.rotate(Vector3f.XP.rotationDegrees(70.0F));
        Matrix4f matrix4f1 = matrixStack.getLast().getMatrix();
        float f12 = 60.0F;
        textureManager.bindTexture(VORTEX_TEXTURE);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(matrix4f1, -f12, 100.0F, -f12).tex(0.0F, 0.0F).endVertex();
        bufferbuilder.pos(matrix4f1, f12, 100.0F, -f12).tex(1.0F, 0.0F).endVertex();
        bufferbuilder.pos(matrix4f1, f12, 100.0F, f12).tex(1.0F, 1.0F).endVertex();
        bufferbuilder.pos(matrix4f1, -f12, 100.0F, f12).tex(0.0F, 1.0F).endVertex();
        bufferbuilder.finishDrawing();
        WorldVertexBufferUploader.draw(bufferbuilder);
        RenderSystem.disableTexture();
        RenderSystem.color4f(50, 50, 50, 50);
        this.starVBO.bindBuffer();
        this.skyVertexFormat.setupBufferState(0L);
        this.starVBO.draw(matrixStack.getLast().getMatrix(), 7);
        VertexBuffer.unbindBuffer();
        this.skyVertexFormat.clearBufferState();

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableFog();
        matrixStack.pop();
        RenderSystem.disableTexture();

        float red, green, blue;

        if (world.dimension.isSkyColored()) {
            red = f * 0.2F + 0.04F;
            green = f1 * 0.2F + 0.04F;
            blue = f2 * 0.6F + 0.1F;
        }
        else {
            red = f;
            green = f1;
            blue = f2;
        }

        RenderSystem.color3f(red, green, blue);

        RenderSystem.enableTexture();
        RenderSystem.depthMask(true);
        RenderSystem.disableFog();
    }

}

