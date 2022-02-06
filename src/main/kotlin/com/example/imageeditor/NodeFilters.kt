package com.example.imageeditor

import javafx.embed.swing.SwingFXUtils
import javafx.fxml.FXML
import javafx.scene.image.Image
import javafx.scene.image.WritablePixelFormat
import javafx.scene.input.DataFormat
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.awt.Color
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

class Blur(nodeState: DataFormat, linkState: DataFormat, id: UInt): Filter(nodeState, linkState, id) {
    private lateinit var kernelSizeValue: InputLink<Int?>

    @FXML
    override fun setTitle() { nodeName.text = "Blur" }

    override fun initialize() {
        super.initialize()
        kernelSizeValue = InputLink(null, this)
        inputs = mapOf(Pair(kernelSizeValue, "Kernel size"))
        initInputs()
        addInputs(3)
        bindInputs()
    }

    override fun filterFunction(img: Image): Image {
        val src = imageToMat(img)
        val res = Mat()
        var kernelSize = kernelSizeValue.valueProperty.value!!
        if (kernelSize % 2 == 0) kernelSize++
        Imgproc.GaussianBlur(src, res, Size(kernelSize.toDouble(), kernelSize.toDouble()), 0.0)
        return matToImage(res)
    }

    override fun initType(): String = BlurNodeType

    override fun initInputs() { linkInputs.addAll(listOf(inputImage, kernelSizeValue))}
}


fun imageToMat(image: Image): Mat {
    val width: Int = image.width.toInt()
    val height: Int = image.height.toInt()
    val buffer = ByteArray(width * height * 4)
    val pixelReader = image.pixelReader
    val format = WritablePixelFormat.getByteBgraInstance()
    pixelReader.getPixels(0, 0, width, height, format, buffer, 0, width * 4)
    val mat = Mat(height, width, CvType.CV_8UC4)
    mat.put(0, 0, buffer)
    return mat
}

fun matToImage(mat: Mat): Image {
    val buffer = MatOfByte()
    Imgcodecs.imencode(".png", mat, buffer)
    return Image(ByteArrayInputStream(buffer.toArray()))
}

class Brightness(nodeState: DataFormat, linkState: DataFormat, id: UInt) : Filter(nodeState, linkState, id) {
    private lateinit var brightnessLevel: InputLink<Float?>

    @FXML
    override fun setTitle() { nodeName.text = "Brightness" }
    override fun initialize() {
        super.initialize()
        brightnessLevel = InputLink(null, this)
        inputs = mapOf(Pair(brightnessLevel, "Level"))
        initInputs()
        addInputs(3)
        bindInputs()
    }

    override fun filterFunction(img: Image): Image {
        val resultMat = imageToMat(img)
        imageToMat(img).convertTo(resultMat, -1, 1.0,
            brightnessLevel.valueProperty.value!!.toDouble() * 100)
        return matToImage(resultMat)
    }

    override fun initType(): String = BrightnessNodeType

    override fun initInputs() { linkInputs.addAll(listOf(inputImage, brightnessLevel)) }
}

class Gray(nodeState: DataFormat, linkState: DataFormat, id: UInt): Filter(nodeState, linkState, id) {

    @FXML
    override fun setTitle() { nodeName.text = "Gray Filter" }

    override fun initialize() {
        super.initialize()
        inputs = mapOf()
        initInputs()
    }

    override fun filterFunction(img: Image): Image {
        val tmpMat = imageToMat(img)
        val resultMat = Mat()
        Imgproc.cvtColor(tmpMat, resultMat, Imgproc.COLOR_RGB2GRAY)
        return matToImage(resultMat)
    }

    override fun initType(): String = GrayFilterNodeType

    override fun initInputs() { linkInputs.addAll(listOf(inputImage)) }
}

class Invert(nodeState: DataFormat, linkState: DataFormat, id: UInt): Filter(nodeState, linkState, id) {

    @FXML
    override fun setTitle() { nodeName.text = "Invert" }

    override fun initialize() {
        super.initialize()
        inputs = mapOf()
        initInputs()
    }

    override fun filterFunction(img: Image): Image {
        val bufferedImage = SwingFXUtils.fromFXImage(img, null)
        for (x in 0 until bufferedImage.width) {
            for (y in 0 until bufferedImage.height) {
                val rgba = bufferedImage.getRGB(x, y)
                val color = Color(rgba, true)
                val invertedColor = Color(255 - color.red, 255 - color.green, 255 - color.blue)
                bufferedImage.setRGB(x, y, invertedColor.rgb)
            }
        }
        return SwingFXUtils.toFXImage(bufferedImage, null)
    }

    override fun initType(): String = InvertNodeType

    override fun initInputs() { linkInputs.addAll(listOf(inputImage)) }
}

class Move(nodeState: DataFormat, linkState: DataFormat, id: UInt): Filter(nodeState, linkState, id){
    private lateinit var x: InputLink<Float?>
    private lateinit var y: InputLink<Float?>

    @FXML
    override fun setTitle() { nodeName.text = "Move" }

    override fun initialize() {
        super.initialize()
        x = InputLink(null, this)
        y = InputLink(null, this)
        inputs = mapOf(Pair(x, "float x"), Pair(y, "float y"))
        initInputs()
        addInputs(3)
        bindInputs()
    }

    override fun filterFunction(img: Image): Image {
        val mat = imageToMat(img)
        val translateMat = Mat(2, 3, CvType.CV_64FC1)
        translateMat.put(0, 0, 1.0, 0.0, x.valueProperty.value!!.toDouble(), 0.0, 1.0,
            y.valueProperty.value!!.toDouble())
        Imgproc.warpAffine(mat, mat, translateMat, mat.size())
        return matToImage(mat)
    }

    override fun initType(): String = MoveNodeType

    override fun initInputs() { linkInputs.addAll(listOf(inputImage, x, y)) }

}

class Rotation(nodeState: DataFormat, linkState: DataFormat, id: UInt): Filter(nodeState, linkState, id){
    private lateinit var angle: InputLink<Float?>

    @FXML
    override fun setTitle() { nodeName.text = "Rotate" }

    override fun initialize() {
        super.initialize()
        angle = InputLink(null, this)
        inputs = mapOf(Pair(angle, "Angle"))
        initInputs()
        addInputs(3)
        bindInputs()
    }

    override fun filterFunction(img: Image): Image {
        val bufferedImage = SwingFXUtils.fromFXImage(img, null)
        val rad = Math.toRadians(angle.valueProperty.value!!.toDouble())
        val sin = abs(sin(rad))
        val cos = abs(cos(rad))
        val width = floor(bufferedImage.width * cos + bufferedImage.height * sin).toInt()
        val height = floor(bufferedImage.height * cos + bufferedImage.width * sin).toInt()
        val rotatedImage = BufferedImage(width, height, bufferedImage.type)
        val affineTransform = AffineTransform()
        affineTransform.translate(width / 2.0, height / 2.0)
        affineTransform.rotate(rad, 0.0, 0.0)
        affineTransform.translate(-bufferedImage.width / 2.0, -bufferedImage.height / 2.0)
        val rotateOp = AffineTransformOp(affineTransform, AffineTransformOp.TYPE_BILINEAR)
        rotateOp.filter(bufferedImage, rotatedImage)
        return SwingFXUtils.toFXImage(rotatedImage, null)
    }

    override fun initType(): String = RotationNodeType

    override fun initInputs() { linkInputs.addAll(listOf(inputImage, angle)) }
}

class Scale(nodeState: DataFormat, linkState: DataFormat, id: UInt): Filter(nodeState, linkState, id){
    private lateinit var x: InputLink<Float?>
    private lateinit var y: InputLink<Float?>

    @FXML
    override fun setTitle() { nodeName.text = "Scale" }

    override fun initialize() {
        super.initialize()
        x = InputLink(null, this)
        y = InputLink(null, this)
        inputs = mapOf(Pair(x, "float x"), Pair(y, "float y"))
        initInputs()
        addInputs(3)
        bindInputs()
    }

    override fun filterFunction(img: Image): Image {
        val bufferedImage = SwingFXUtils.fromFXImage(img, null)
        val xFactor = x.valueProperty.value!!
        val yFactor = y.valueProperty.value!!
        var scaledImage = BufferedImage(floor(bufferedImage.width * xFactor).toInt(),
            floor(bufferedImage.height * yFactor).toInt(), BufferedImage.TYPE_INT_ARGB)
        val affineTransform = AffineTransform.getScaleInstance(xFactor.toDouble(), yFactor.toDouble())
        val scaleOp = AffineTransformOp(affineTransform, AffineTransformOp.TYPE_BICUBIC)
        scaledImage = scaleOp.filter(bufferedImage, scaledImage)
        return SwingFXUtils.toFXImage(scaledImage, null)
    }

    override fun initType(): String = ScaleNodeType

    override fun initInputs() { linkInputs.addAll(listOf(inputImage, x, y)) }
}

class Sepia(nodeState: DataFormat, linkState: DataFormat, id: UInt): Filter(nodeState, linkState, id) {
    private lateinit var mSepiaKernel: Mat

    @FXML
    override fun setTitle() { nodeName.text = "Sepia" }
    override fun initialize() {
        super.initialize()
        mSepiaKernel =  Mat(4, 4, CvType.CV_32F)
        mSepiaKernel.put(0, 0, /* R */0.272, 0.534, 0.131, 0.0)
        mSepiaKernel.put(1, 0, /* G */0.349, 0.686, 0.168, 0.0)
        mSepiaKernel.put(2, 0, /* B */0.393, 0.769, 0.189, 0.0)
        mSepiaKernel.put(3, 0, /* A */0.000, 0.000, 0.000, 1.0)
        inputs = mapOf()
        initInputs()
    }

    override fun filterFunction(img: Image): Image {
        val bufMat: Mat = imageToMat(img)
        val resultMat = Mat()
        Core.transform(bufMat, resultMat, mSepiaKernel)
        return matToImage(resultMat)
    }

    override fun initType(): String = SepiaNodeType

    override fun initInputs() { linkInputs.addAll(listOf(inputImage)) }
}
