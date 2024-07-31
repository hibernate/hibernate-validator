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

def removeDependency(File file, String dependencyToRemove) {
    file.write( file.text.replaceAll( /<module name="${dependencyToRemove}"[^\/]*\/>/, '' ) )
}

// Jakarta Validation API
bvModuleXml = new File( wildflyPatchedTargetDir, 'modules/system/layers/base/jakarta/validation/api/main/module.xml' )
def bvArtifactName = 'jakarta.validation-api-' + project.properties['version.jakarta.validation-api'] + '.jar';
println "[INFO] Using Jakarta Validation version " + bvArtifactName;
processFileInplace( bvModuleXml ) { text ->
    text.replaceAll( /<resource-root path=".*validation-api.*jar/, '<resource-root path="' + bvArtifactName )
}

deleteFiles( new FileNameByRegexFinder().getFileNames( wildflyPatchedTargetDir + '/modules/system/layers/base/jakarta/validation/api/main', '.*\\.jar' ) )

// HV
hvModuleXml = new File( wildflyPatchedTargetDir, 'modules/system/layers/base/org/hibernate/validator/main/module.xml' )
def hvArtifactName = 'hibernate-validator-' + project.version + '.jar';
println "[INFO] Using HV version " + hvArtifactName;
processFileInplace( hvModuleXml ) { text ->
    text.replaceAll( /hibernate-validator.*jar/, hvArtifactName )
}

removeDependency( hvModuleXml, "org.apache.xerces" )
appendDependency( hvModuleXml, "javax.xml.stream.api", false )
appendDependency( hvModuleXml, "javax.api", false )

appendDependency( hvModuleXml, "javax.money.api", true )
appendDependency( hvModuleXml, "javafx.api", true )

deleteFiles( new FileNameByRegexFinder().getFileNames( wildflyPatchedTargetDir + '/modules/system/layers/base/org/hibernate/validator/main', 'hibernate-validator-.*\\.jar' ) )

// HV CDI
hvCdiModuleXml = new File( wildflyPatchedTargetDir, 'modules/system/layers/base/org/hibernate/validator/cdi/main/module.xml' )
def hvCdiArtifactName = 'hibernate-validator-cdi-' + project.version + '.jar';
println "[INFO] Using HV CDI Portable Extension version " + hvCdiArtifactName;
processFileInplace( hvCdiModuleXml ) { text ->
    text.replaceAll( /hibernate-validator-cdi.*jar/, hvCdiArtifactName )
}

deleteFiles( new FileNameByRegexFinder().getFileNames( wildflyPatchedTargetDir + '/modules/system/layers/base/org/hibernate/validator/cdi/main', 'hibernate-validator-cdi-.*\\.jar' ) )

// JBoss Logging
jbossLoggingModuleXml = new File( wildflyPatchedTargetDir, 'modules/system/layers/base/org/jboss/logging/main/module.xml' )
def jbossArtifactName = 'jboss-logging-' + jbossLoggingVersion + '.jar';
println "[INFO] Using JBoss Logging version " + jbossArtifactName;
processFileInplace( jbossLoggingModuleXml ) { text ->
    text.replaceAll( /jboss-logging.*jar/, jbossArtifactName )
}

deleteFiles( new FileNameByRegexFinder().getFileNames( wildflyPatchedTargetDir + '/modules/system/layers/base/org/jboss/logging/main', 'jboss-logging-.*\\.jar' ) )

println "[INFO] ------------------------------------------------------------------------";
