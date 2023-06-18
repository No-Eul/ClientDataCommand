plugins {
	id("java")
	id("fabric-loom") version "latest.release"
	id("maven-publish")
}

group = "dev.noeul.fabricmod"
version = property("mod_version")!!

repositories {
	mavenCentral()
	maven("https://maven.terraformersmc.com/releases")
	maven("https://maven.shedaniel.me/")
	maven("https://api.modrinth.com/maven") { name = "Modrinth" }
	maven("https://jitpack.io")
}


dependencies {
	minecraft("com.mojang:minecraft:${property("minecraft_version")}")
	mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
	modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

	modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
	modImplementation("com.terraformersmc:modmenu:${property("mod_menu_version")}")

	modRuntimeOnly("maven.modrinth:mixintrace:1.1.1+1.17") // https://modrinth.com/mod/mixintrace/versions
	modRuntimeOnly("maven.modrinth:spark:1.10.73-fabric") { // https://modrinth.com/mod/spark/versions
		modRuntimeOnly("me.lucko:fabric-permissions-api:0.3.1")
	}
	modRuntimeOnly("maven.modrinth:language-reload:1.6.1+1.21") // https://modrinth.com/mod/language-reload/versions
	modRuntimeOnly("maven.modrinth:ferrite-core:7.0.0") // https://modrinth.com/mod/ferrite-core/versions
	modRuntimeOnly("maven.modrinth:auth-me:8.0.0+1.21") { // https://modrinth.com/mod/auth-me/versions
		// https://linkie.shedaniel.dev/dependencies?loader=fabric&version=1.21#dep-2
		modRuntimeOnly("me.shedaniel.cloth:cloth-config-fabric:15.+")
	}
}

loom {
	sourceSets.forEach {
		it.resources.files
			.find { file -> file.name.endsWith(".accesswidener") }
			?.let(accessWidenerPath::set)
	}

	@Suppress("UnstableApiUsage")
	mixin {
		defaultRefmapName.set("${property("mod_id")}.refmap.json")
	}

	runs {
		getByName("client") {
			configName = "Minecraft Client"
			runDir = "run/client"
			client()
		}

		getByName("server") {
			configName = "Minecraft Server"
			runDir = "run/server"
			server()
		}
	}
}

val targetJavaVersion: JavaVersion = JavaVersion.toVersion(property("target_java_version")!!)
java {
	targetCompatibility = targetJavaVersion
	sourceCompatibility = targetJavaVersion
	if (JavaVersion.current() < targetJavaVersion)
		toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion.majorVersion.toInt()))
}

tasks {
	compileJava {
		if (targetJavaVersion.majorVersion.toInt() >= 10 || JavaVersion.current().isJava10Compatible)
			options.release.set(targetJavaVersion.majorVersion.toInt())
		this.options.encoding = "UTF-8"
	}

	processResources {
		val inputs = mapOf(
			"mod_id" to project.property("mod_id"),
			"version" to project.version,
			"name" to project.property("mod_name"),
			"java_version" to project.property("target_java_version"),
			"minecraft_version" to project.property("minecraft_version"),
			"loader_version" to project.property("loader_version"),
			"fabric_api_version" to project.property("fabric_api_version"),
		)
		this.inputs.properties(inputs)
		filesMatching("fabric.mod.json") {
			expand(inputs)
		}
	}

	jar {
		from("LICENSE.txt")
		archiveBaseName.set("${project.property("archive_base_name")}")
		archiveAppendix.set("fabric")
		archiveVersion.set("${project.version}+mc${project.property("minecraft_version")}")
	}

	remapJar {
		archiveBaseName.set("${project.property("archive_base_name")}")
		archiveAppendix.set("fabric")
		archiveVersion.set("${project.version}+mc${project.property("minecraft_version")}")
	}
}

afterEvaluate {
	loom.runs.configureEach {
		vmArgs(
			"-Dfabric.systemLibraries=${System.getProperty("java.home")}/lib/hotswap/hotswap-agent.jar",
			"-Dfabric.development=true",
			"-Dfabric.fabric.debug.deobfuscateWithClasspath",
			"-Dmixin.debug.export=true",
			"-Dmixin.debug.verify=true",
//			"-Dmixin.debug.strict=true",
			"-Dmixin.debug.countInjections=true",
			"-Dmixin.hotSwap=true",
			"-XX:+AllowEnhancedClassRedefinition",
			"-XX:HotswapAgent=fatjar",
			"-XX:+IgnoreUnrecognizedVMOptions",
			"-javaagent:${
				configurations.compileClasspath.get()
					.files { it.group == "net.fabricmc" && it.name == "sponge-mixin" }
					.first()
			}"
		)
	}
}

// configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
	}
}
