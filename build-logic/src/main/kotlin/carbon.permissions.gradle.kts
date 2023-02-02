val ext = extensions.create("carbonPermission", CarbonPermissionsExtension::class.java)
ext.yaml.convention(rootProject.layout.projectDirectory.file("common/src/main/resources/carbon-permissions.yml"))
