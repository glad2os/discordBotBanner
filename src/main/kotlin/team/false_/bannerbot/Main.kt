package team.false_.bannerbot

import club.minnced.jda.reactor.ReactiveEventManager
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Icon
import net.dv8tion.jda.api.events.ReadyEvent
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import kotlin.concurrent.timerTask


object Main {
    private const val GUILD = 381730844588638208
    private const val PERIOD = 3000L

    private const val WIDTH = 960
    private const val HEIGHT = 540
    private const val OFFSET = HEIGHT - 32

    private val template = ImageIO.read(Main::class.java.getResource("/template.png"))
    private val font = Font("TimesRoman", Font.BOLD, 70)

    private val timer = Timer(true)

    private fun generate(count: Int): ByteArray {
        val message = "Online $count"

        val image = BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB)

        with(image.createGraphics()) {
            this.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            this.drawImage(template, AffineTransform(), null)
            this.font = Main.font
            this.paint = Color.decode("#d2393a");
            this.drawString(message, (WIDTH - this.fontMetrics.stringWidth(message)) / 2, OFFSET)
        }

        val outputfile = File("image.png")
        ImageIO.write(image, "png", outputfile)

        return ByteArrayOutputStream()
            .apply { ImageIO.write(image, "png", this) }
            .toByteArray()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val manager = ReactiveEventManager()
        manager.on(ReadyEvent::class.java)
            .subscribe {
                val guild = it.jda.getGuildById(GUILD)!!
                timer.schedule(timerTask {
                    val count = guild.voiceChannels.map { it.members.count() }.sum()
                    guild.manager.setBanner(Icon.from(generate(count))).submit()
                }, 0, PERIOD)
            }

        val jda = JDABuilder.createDefault(System.getenv("TOKEN"))
            .setEventManager(manager)
            .build()

        Runtime.getRuntime().addShutdownHook(Thread {
            jda.shutdown()
        })
    }
}