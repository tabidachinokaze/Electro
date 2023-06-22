package cn.tabidachi.electro.coil

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.HardwareRenderer
import android.graphics.PixelFormat
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.hardware.HardwareBuffer
import android.media.ImageReader
import android.os.Build
import androidx.annotation.RequiresApi
import coil.size.Size
import coil.transform.Transformation

@RequiresApi(Build.VERSION_CODES.S)
class BlurTransformation(
    private val radius: Float,
    private val sampling: Float
) : Transformation {
    override val cacheKey: String = "${BlurTransformation::class.java.name}-$radius-$sampling"

    @SuppressLint("WrongConstant")
    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val renderNode = RenderNode("RenderEffect")
        val hardwareRenderer = HardwareRenderer()
        val imageReader = ImageReader.newInstance(
            input.width,
            input.height,
            PixelFormat.RGBA_8888,
            1,
            HardwareBuffer.USAGE_GPU_SAMPLED_IMAGE or HardwareBuffer.USAGE_GPU_COLOR_OUTPUT
        )
        hardwareRenderer.setSurface(imageReader.surface)
        hardwareRenderer.setContentRoot(renderNode)
        renderNode.setPosition(0, 0, imageReader.width, imageReader.height)
        val blurEffect = RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.MIRROR)
        renderNode.setRenderEffect(blurEffect)
        val recordingCanvas = renderNode.beginRecording()
        recordingCanvas.drawBitmap(input, 0f, 0f, null)
        renderNode.endRecording()
        hardwareRenderer.createRenderRequest()
            .setWaitForPresent(true)
            .syncAndDraw()
        val image = imageReader.acquireNextImage() ?: throw RuntimeException("No Image")
        val hardwareBuffer = image.hardwareBuffer ?: throw RuntimeException("No HardwareBuffer")
        val bitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, null)
            ?: throw RuntimeException("Create bitmap Failed")
        hardwareBuffer.close()
        image.close()
        imageReader.close()
        renderNode.discardDisplayList()
        hardwareRenderer.destroy()
        return bitmap
    }
}