// Needed while running against an AS instance which is not bundled with HV 5
println "[INFO] ------------------------------------------------------------------------";
println "[INFO] UPDATING DEFAULT JBOSS AS MODULES                                       ";
println "[INFO] ------------------------------------------------------------------------";

def updateModule(Map args) {
    // Ensure any expected parameters are defined
    ['from','to', 'deleteBeforeCopy', 'version'].each{ args.get(it,'') }

    if (args.deleteBeforeCopy) {
        log.info('Deleting ' + args.from )
        ant.delete(dir : args.to)
    }

    ant.copy(todir: args.to, filtering: true) {
        fileset(dir: args.from)
        filterset() {
            filter(token:'VERSION', value:args.version)
        }
    }
}

println "[INFO] --- Updating Bean Validation API module";
def toDir = new File(project.properties['jbossTargetDir'], 'modules/javax/validation')
def fromDir = new File(project.basedir, 'src/as7config/modules/javax/validation')
updateModule(from:fromDir, to:toDir, deleteBeforeCopy:true, version:project.properties['bv.api.version'])

println "[INFO] --- Updating Hibernate Validator module";
toDir = new File(project.properties['jbossTargetDir'], 'modules/org/hibernate/validator')
fromDir = new File(project.basedir, 'src/as7config/modules/org/hibernate/validator')
updateModule(from:fromDir, to:toDir, deleteBeforeCopy:true, version:project.version)

println "[INFO] --- Creating classmate module";
toDir = new File(project.properties['jbossTargetDir'], 'modules/com/fasterxml')
fromDir = new File(project.basedir, 'src/as7config/modules/com/fasterxml')
updateModule(from:fromDir, to:toDir, deleteBeforeCopy:false, version:project.properties['classmate.version'])

println "[INFO] ------------------------------------------------------------------------";