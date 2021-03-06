// Web Plugin for Jetty
resolvers += "Web plugin repo" at "http://siasia.github.com/maven2"

libraryDependencies <+= sbtVersion(v => v match {
	case "0.11.0" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.0-0.2.8"
	case "0.11.1" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.1-0.2.10"
	case "0.11.2" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.2-0.2.11"
	case "0.11.3" => "com.github.siasia" %% "xsbt-web-plugin" % "0.11.3-0.2.11.1"
    case x if (x.startsWith("0.12")) => "com.github.siasia" %% "xsbt-web-plugin" % "0.12.0-0.2.11.1"
})

// CoffeeScript plugin
resolvers += Resolver.url("untyped", url("http://ivy.untyped.com"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.untyped" %% "sbt-js" % "0.5-M9")

// StartScript plugin for Crawler (https://github.com/typesafehub/xsbt-start-script-plugin)
resolvers += Classpaths.typesafeResolver

addSbtPlugin("com.typesafe.startscript" % "xsbt-start-script-plugin" % "0.5.2")

// IntelliJ IDEA plugin
resolvers += "sbt-idea-repo" at "http://mpeltonen.github.com/maven/"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.1.0")

