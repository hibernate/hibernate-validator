println "[INFO] ------------------------------------------------------------------------";
println "[INFO] UPDATING BEAN VALIDATION RELATED WILDFLY MODULES                        ";
println "[INFO] ------------------------------------------------------------------------";

def processFileInplace(File file, Closure processText) {
    def text = file.text
    file.write( processText( text ) )
}

def appendDependency(File file, String dependencyToAppend, boolean optional) {
    file.write( file.text.replaceAll( /<\/dependencies>/, '  <module name="' + dependencyToAppend + '"' + ( optional ? ' optional="true"' : '' ) + '/>\n  </dependencies>' ) )
}

new File( project.properties['wildflyPatchedTargetDir'], 'modules/system/layers/base/javax/validation/api/main/validation-api-1.1.0.Final.jar' ).delete()

// HV
hvModuleXml = new File( project.properties['wildflyPatchedTargetDir'], 'modules/system/layers/base/org/hibernate/validator/main/module.xml' )
def hvArtifactName = 'hibernate-validator-' + project.version + '.jar';
println "[INFO] Using HV version " + hvArtifactName;
processFileInplace( hvModuleXml ) { text ->
    text.replaceAll( /hibernate-validator.*jar/, hvArtifactName )
}
appendDependency( hvModuleXml, "javax.money.api", true )

new File( project.properties['wildflyPatchedTargetDir'], 'modules/system/layers/base/org/hibernate/validator/main/hibernate-validator-5.2.4.Final.jar' ).delete()

// HV CDI
hvCdiModuleXml = new File( project.properties['wildflyPatchedTargetDir'], 'modules/system/layers/base/org/hibernate/validator/cdi/main/module.xml' )
def hvCdiArtifactName = 'hibernate-validator-cdi-' + project.version + '.jar';
println "[INFO] Using HV CDI Portable Extension version " + hvCdiArtifactName;
processFileInplace( hvCdiModuleXml ) { text ->
    text.replaceAll( /hibernate-validator-cdi.*jar/, hvCdiArtifactName )
}

new File( project.properties['wildflyPatchedTargetDir'], 'modules/system/layers/base/org/hibernate/validator/cdi/main/hibernate-validator-cdi-5.2.4.Final.jar' ).delete()

println "[INFO] ------------------------------------------------------------------------";
