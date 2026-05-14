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
appendDependency( hvModuleXml, "org.hibernate.accessor", false )
appendDependency( hvModuleXml, "javax.xml.stream.api", false )
appendDependency( hvModuleXml, "javax.api", false )

appendDependency( hvModuleXml, "javax.money.api", true )
appendDependency( hvModuleXml, "javafx.api", true )

deleteFiles( new FileNameByRegexFinder().getFileNames( wildflyPatchedTargetDir + '/modules/system/layers/base/org/hibernate/validator/main', 'hibernate-validator-.*\\.jar' ) )

// Hibernate Accessor
def haModuleDir = new File( wildflyPatchedTargetDir, 'modules/system/layers/base/org/hibernate/accessor/main' )
haModuleDir.mkdirs()

def haArtifactName = 'hibernate-accessor-core-' + hibernateAccessorVersion + '.jar';
println "[INFO] Using Hibernate Accessor version " + haArtifactName;

new File( haModuleDir, 'module.xml' ).text =
        """<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ SPDX-License-Identifier: Apache-2.0
  ~ Copyright Red Hat Inc. and Hibernate Authors
  -->
<module name="org.hibernate.accessor" xmlns="urn:jboss:module:1.9">
  <resources>                                                                                                                                                                                                   
      <resource-root path="${haArtifactName}" />
  </resources>                                                                                                                                                                                                  
</module>                                                       
"""

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

// Byte Buddy
byteBuddyModuleXml = new File( wildflyPatchedTargetDir, '/modules/system/layers/base/net/bytebuddy/main/module.xml' )
def byteBuddyArtifactName = 'byte-buddy-' + byteBuddyVersion + '.jar';
println "[INFO] Using Byte Buddy version " + byteBuddyArtifactName;
processFileInplace( byteBuddyModuleXml ) { text ->
	text.replaceAll( /byte-buddy.*jar/, byteBuddyArtifactName )
}

deleteFiles( new FileNameByRegexFinder().getFileNames( wildflyPatchedTargetDir + '/modules/system/layers/base/net/bytebuddy/main', 'byte-buddy-.*\\.jar' ) )

println "[INFO] ------------------------------------------------------------------------";
