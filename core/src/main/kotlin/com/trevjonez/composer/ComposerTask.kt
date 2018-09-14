/*
 *    Copyright 2017 Trevor Jones
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.trevjonez.composer

import com.trevjonez.composer.ComposerConfig.MAIN_CLASS
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.Optional
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import java.io.File

//TODO: use the worker api not JavaExec
open class ComposerTask : JavaExec(), ComposerConfigurator, ComposerTaskDsl {

    override val configuration = project.composerConfig()

    @get:[Optional Input]
    override val globalConfig = project.objects.property<ComposerDsl>()

    override val apk = this.newInputFile()

    override val testApk = this.newInputFile()

    override val outputDirectory = this.newOutputDirectory().apply {
        set(project.file(ComposerConfig.DEFAULT_OUTPUT_DIR))
    }

    @get:[Optional Input]
    override val shard = project.objects.property<Boolean>()

    @get:[Optional Input]
    override val instrumentationArguments =
            project.objects.listProperty<Pair<String, String>>()

    @get:[Optional Input]
    override val verboseOutput = project.objects.property<Boolean>()

    @get:[Optional Input]
    override val devices = project.objects.listProperty<String>()

    @get:[Optional Input]
    override val devicePattern = project.objects.property<String>()

    @get:[Optional Input]
    override val keepOutput = project.objects.property<Boolean>()

    @get:[Optional Input]
    override val apkInstallTimeout = project.objects.property<Int>()

    override fun exec() {
        val outputDir = outputDirectory.get().asFile

        if (outputDir.exists()) {
            if (!outputDir.deleteRecursively()) {
                throw IllegalStateException("Failed to remove existing outputs")
            }
        }

        val config = ComposerParams(
                apk.asFile.get(),
                testApk.asFile.get(),
                shard.orNull,
                outputDir,
                instrumentationArguments.orEmpty,
                verboseOutput.orNull,
                devices.orEmpty,
                devicePattern.orNull,
                keepOutput.orNull,
                apkInstallTimeout.orNull)
        args = config.toCliArgs()
        main = MAIN_CLASS
        classpath = configuration

        try {
            super.exec()
        } finally {
            val htmlReportIndex = File(
                    outputDir,
                    "${File.separator}html-report${File.separator}index.html")

            if (htmlReportIndex.exists()) {
                println("\nComposer Html Report: file://${htmlReportIndex.absolutePath}\n")
            }
        }
    }

    override fun apk(path: Any) {
        apk.set(project.file(path))
    }

    override fun testApk(path: Any) {
        testApk.set(project.file(path))
    }

    override fun outputDirectory(path: Any) {
        outputDirectory.set(project.file(path))
    }

    override fun shard(value: Any) {
        shard.eval(value)
    }

    override fun instrumentationArgument(value: Any) {
        instrumentationArguments.eval(value)
    }

    override fun instrumentationArguments(value: Any) {
        instrumentationArguments.evalAll(value)
    }

    override fun verboseOutput(value: Any) {
        verboseOutput.eval(value)
    }

    override fun device(value: Any) {
        devices.eval(value)
    }

    override fun devices(value: Any) {
        devices.evalAll(value)
    }

    override fun devicePattern(value: Any) {
        devicePattern.eval(value)
    }

    override fun keepOutput(value: Any) {
        keepOutput.eval(value)
    }

    override fun apkInstallTimeout(value: Any) {
        apkInstallTimeout.eval(value)
    }
}
