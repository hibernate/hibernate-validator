println "[INFO] ------------------------------------------------------------------------";
println "[INFO] UPDATING BEAN VALIDATION RELATED WILDFLY MODULES                        ";
println "[INFO] ------------------------------------------------------------------------";

def processFileInplace(File file, Closure processText) {
    def text = file.text
    file.write( processText( text ) )
}

def deleteFiles(List<String> filePaths) {
    for ( String filePath : filePaths ) {
        new File( filePath ).delete();
    }
}

def appendDependency(File file, String dependencyToAppend, boolean optional) {
    file.write( file.text.replaceAll( /<\/dependencies>/, '  <module name="' + dependencyToAppend + '"' + ( optional ? ' optional="true"' : '' ) + '/>\n  </dependencies>' ) )
}

// BV API
bvModuleXml = new File( wildflyPatchedTargetDir, 'modules/system/layers/base/javax/validation/api/main/module.xml' )
def bvArtifactName = 'validation-api-' + project.properties['bv.api.version'] + '.jar';
println "[INFO] Using BV version " + bvArtifactName;
processFileInplace( bvModuleXml ) { text ->
    text.replaceAll( /validation-api.*jar/, bvArtifactName )
}

deleteFiles( new FileNameFinder().getFileNames( wildflyPatchedTargetDir + '/modules/system/layers/base/javax/validation/api/main', 'validation-api-*.jar' ) )

// HV
hvModuleXml = new File( wildflyPatchedTargetDir, 'modules/system/layers/base/org/hibernate/validator/main/module.xml' )
def hvArtifactName = 'hibernate-validator-' + project.version + '.jar';
println "[INFO] Using HV version " + hvArtifactName;
processFileInplace( hvModuleXml ) { text ->
    text.replaceAll( /hibernate-validator.*jar/, hvArtifactName )
}
appendDependency( hvModuleXml, "javax.money.api", true )
appendDependency( hvModuleXml, "javafx.api", true )

deleteFiles( new FileNameFinder().getFileNames( wildflyPatchedTargetDir + '/modules/system/layers/base/org/hibernate/validator/main', 'hibernate-validator-*.jar' ) )

// HV CDI
hvCdiModuleXml = new File( wildflyPatchedTargetDir, 'modules/system/layers/base/org/hibernate/validator/cdi/main/module.xml' )
def hvCdiArtifactName = 'hibernate-validator-cdi-' + project.version + '.jar';
println "[INFO] Using HV CDI Portable Extension version " + hvCdiArtifactName;
processFileInplace( hvCdiModuleXml ) { text ->
    text.replaceAll( /hibernate-validator-cdi.*jar/, hvCdiArtifactName )
}

deleteFiles( new FileNameFinder().getFileNames( wildflyPatchedTargetDir + '/modules/system/layers/base/org/hibernate/validator/cdi/main', 'hibernate-validator-cdi-*.jar' ) )

println "[INFO] ------------------------------------------------------------------------";
