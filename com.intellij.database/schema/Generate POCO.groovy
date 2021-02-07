import com.intellij.database.model.DasTable
import com.intellij.database.model.ObjectKind
import com.intellij.database.util.Case
import com.intellij.database.util.DasUtil

typeMapping = [
        (~/(?i)^bit$|tinyint\(1\)/)                                      : "bool",
        (~/(?i)^tinyint$/)                                               : "byte",
        (~/(?i)^smallint$/)                                              : "short",
        (~/(?i)^uniqueidentifier|uuid$/)                                 : "Guid",
        (~/(?i)^int|integer$/)                                           : "int",
        (~/(?i)^bigint$/)                                                : "long",
        (~/(?i)^binary|timestamp|varbinary|image|rowversion|filestream$/): "byte[]",
        (~/(?i)^double|float$/)                                          : "double",
        (~/(?i)^real$/)                                                  : "Single",
        (~/(?i)^decimal|money|numeric|smallmoney$/)                      : "decimal",
        (~/(?i)^time$/)                                                  : "TimeSpan",
        (~/(?i)^datetimeoffset$/)                                        : "DateTimeOffset",
        (~/(?i)^datetime|datetime2|date$/)                               : "DateTime",
        (~/(?i)^char$/)                                                  : "char",
        (~/(?i)^nchar|ntext|nvarchar|text|varchar|xml$/)                 : "string",
        (~/(?i)^geography$/)                                             : "Microsoft.SqlServer.Types.SqlGeography",
        (~/(?i)^geometry$/)                                              : "Microsoft.SqlServer.Types.SqlGeometry",
        (~/(?i)^hierarchyid$/)                                           : "Microsoft.SqlServer.Types.SqlHierarchyId",
]

notNullableTypes = ["object", "string", "byte[]"]

FILES.chooseDirectoryAndSave("Choose directory", "Choose where to store generated files") { dir ->
    SELECTION.filter { it instanceof DasTable && it.getKind() == ObjectKind.TABLE }.each { generate(it, dir) }
}

def generate(table, dir) {
    def className = pascalCase(table.getName())
    def fields = calcFields(table)
    new File(dir, className + ".cs").withPrintWriter { out -> generate(out, className, fields, table) }
}

def generate(out, className, fields, table) {
    out.println "using System;"
    out.println "using System.ComponentModel.DataAnnotations;"
    out.println "using System.ComponentModel.DataAnnotations.Schema;"
    out.println ""
    out.println "[Table(\"${table.getName()}\")]"
    out.println "public class $className"
    out.println "{"

    fields.each() {
        if (it.primarykey)
            out.println "    [Key]"

        if (it.comment != "") {
            out.println "";
            out.println "    // ${it.comment}";
        }

        if (it.type == "string") {
            if (!it.nullable)
                out.println "    [Required]";

            if (!it.spec.contains("max") && it.length)
                out.println "    [MaxLength(${it.length})]";
        }

        if (it.type == "byte[]" && it.spec == "timestamp")
            out.println "    [Timestamp]";

        out.println "    public ${it.type} ${it.name} { get; set; }"
    }
    out.println "}"
    out.println ""
}

def calcFields(table) {
    DasUtil.getColumns(table).reduce([]) { fields, col ->
        def spec = Case.LOWER.apply(col.getDataType().getSpecification())
        def typeStr = typeMapping.find { p, t -> p.matcher(spec).find() }?.value ?: "object"
        def nullable = col.isNotNull() || typeStr in notNullableTypes ? "" : "?"
        def pk = DasUtil.getPrimaryKey(table).toString();

        fields += [[
                           primarykey: pk != null && pk != "" && pk.contains("(${col.getName()})") ? true : false,
                           colname   : col.getName(),
                           spec      : spec,
                           name      : pascalCase(col.getName()),
                           type      : typeStr + nullable,
                           comment   : col.comment ? col.comment : "",
                           length    : col.getDataType().getLength(),
                           nullable  : !col.isNotNull()]]
    }
}

def pascalCase(str) {
    com.intellij.psi.codeStyle.NameUtil.splitNameIntoWords(str)
            .collect { Case.LOWER.apply(it).capitalize() }
            .join("")
}
