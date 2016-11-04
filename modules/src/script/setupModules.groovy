println "[INFO] ------------------------------------------------------------------------";
println "[INFO] UPDATING BEAN VALIDATION RELATED WILDFLY MODULES                        ";
println "[INFO] ------------------------------------------------------------------------";

def processFileInplace(file, Closure processText) {
    def text = file.text
    file.write( processText( text ) )
}

// BV API
bvModuleXml = new File( project.properties['wildflyPatchedTargetDir'], 'modules/system/layers/base/javax/validation/api/main/module.xml' )
def bvArtifactName = 'validation-api-' + project.properties['bv.api.version'] + '.jar';
println "[INFO] Using BV version " + bvArtifactName;
processFileInplace( bvModuleXml ) { text ->
    text.replaceAll( /validation-api.*jar/, bvArtifactName )
}

new File( project.properties['wildflyPatchedTargetDir'], 'modules/system/layers/base/javax/validation/api/main/validation-api-1.1.0.Final.jar' ).delete()

// HV
hvModuleXml = new File( project.properties['wildflyPatchedTargetDir'], 'modules/system/layers/base/org/hibernate/validator/main/module.xml' )
def hvArtifactName = 'hibernate-validator-' + project.version + '.jar';
println "[INFO] Using HV version " + hvArtifactName;
processFileInplace( hvModuleXml ) { text ->
    text.replaceAll( /hibernate-validator.*jar/, hvArtifactName )
}

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
