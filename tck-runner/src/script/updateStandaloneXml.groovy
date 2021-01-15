println "[INFO] ------------------------------------------------------------------------";
println "[INFO] UPDATING standalone.xml                                                 ";
println "[INFO] ------------------------------------------------------------------------";

def processFileInplace(File file, Closure processText) {
    def text = file.text
    file.write( processText( text ) )
}

String getPropertyValue(String name) {
    def value = session.userProperties[name]
    if (value != null) return value //property was defined from command line e.g.: -DpropertyName=value
    return project.properties[name]
}

// Add javafx.api module to the global modules
standaloneXml = new File( getPropertyValue('wildfly.target-dir'), 'standalone/configuration/standalone.xml' )
println "[INFO] Add javafx.api as global module"

processFileInplace( standaloneXml ) { text ->
    text.replaceAll( /<subsystem xmlns="urn:jboss:domain:ee:5\.0">/, '<subsystem xmlns="urn:jboss:domain:ee:5.0">\n            <global-modules>\n                <module name="javafx.api" slot="main" />\n            </global-modules>' )
}

println "[INFO] ------------------------------------------------------------------------";
