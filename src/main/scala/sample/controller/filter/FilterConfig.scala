package sample.controller.filter

import org.springframework.context.annotation.Configuration
import sample.context.security.SecurityFilters
import javax.servlet.Filter

/**
 * ServletFilterの拡張実装。
 * filtersで返すFilterはSecurityHandlerにおいてActionSessionFilterの後に定義されます。
 */
@Configuration
class FilterConfig extends SecurityFilters {
  
  override def filters(): Seq[Filter] = Seq()
  
}