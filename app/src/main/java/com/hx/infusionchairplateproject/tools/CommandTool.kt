package com.hx.infusionchairplateproject.tools

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset

/**
 * 命令行工具类
 */
class CommandTool {

    companion object {

        /**
         * 用Root权限执行命令
         * 需设备支持Root权限
         */
        open fun execSuCMD(cmd: String) : String{
            val command = cmd + "\n"
            var process: Process? = null
            var out: DataOutputStream? = null
            var errorStream: BufferedReader? = null
            try {
                // 请求root
                process = Runtime.getRuntime().exec("su")
                out = DataOutputStream(process.outputStream)
                // 调用命令
                out.run {
                    write(command.toByteArray(Charset.forName("utf-8")))
                    flush()
                    writeBytes("exit\n")
                    flush()
                }
                process.waitFor()
                errorStream = BufferedReader(InputStreamReader(process.errorStream))
                var msg = ""
                var line: String
                while (errorStream.readLine().also { line = it } != null) {
                    msg += line
                }
                if (!msg.contains("Failure")) {
                    // success
                    return "success"
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    out?.close()
                    errorStream?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return "fail"
        }

    }
}