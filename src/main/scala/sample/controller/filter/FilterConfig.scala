package sample.controller.filter

import org.springframework.context.annotation.Configuration

import javax.servlet.Filter
import sample.context.security.SecurityFilters

/**
 * ServletFilterの拡張実装。
 * filtersで返すFilterはSecurityHandlerにおいてActionSessionFilterの後に定義されます。
 */
@Configuration
class FilterConfig extends SecurityFilters {
  
  override def filters(): Seq[Filter] = Seq()
  
}