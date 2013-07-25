// Needed while running against an AS instance which is not bundled with HV 5
println "[INFO] ------------------------------------------------------------------------";
println "[INFO] UPDATING BEAN VALIDATION RELATED WILDFLY MODULES                        ";
println "[INFO] ------------------------------------------------------------------------";

def processFileInplace(file, Closure processText) {
    def text = file.text
    file.write( processText( text ) )
}

hvModuleXml = new File( project.properties['jbossTargetDir'], 'modules/system/layers/base/org/hibernate/validator/main/module.xml' )
version = 'hibernate-validator-' + project.version + '.jar';
println "[INFO] Using HV version " + version;
processFileInplace( hvModuleXml ) { text ->
    text.replaceAll( /hibernate-validator-5.0.1.Final.jar/, version )
}

println "[INFO] ------------------------------------------------------------------------";
