println "[INFO] ------------------------------------------------------------------------";
println "[INFO] UPDATING BEAN VALIDATION RELATED WILDFLY MODULES                        ";
println "[INFO] ------------------------------------------------------------------------";

def processFileInplace(file, Closure processText) {
    def text = file.text
    file.write( processText( text ) )
}

hvModuleXml = new File( project.properties['wildflyTargetDir'], 'modules/system/layers/base/org/hibernate/validator/main/module.xml' )
def hvArtifactName = 'hibernate-validator-' + project.version + '.jar';
println "[INFO] Using HV version " + hvArtifactName;
processFileInplace( hvModuleXml ) { text ->
    text.replaceAll( /hibernate-validator.*jar/, hvArtifactName )
}

hvCdiModuleXml = new File( project.properties['wildflyTargetDir'], 'modules/system/layers/base/org/hibernate/validator/cdi/main/module.xml' )
def hvCdiArtifactName = 'hibernate-validator-cdi-' + project.version + '.jar';
println "[INFO] Using HV CDI Portable Extension version " + hvCdiArtifactName;
processFileInplace( hvCdiModuleXml ) { text ->
    text.replaceAll( /hibernate-validator-cdi.*jar/, hvCdiArtifactName )
}

println "[INFO] ------------------------------------------------------------------------";
