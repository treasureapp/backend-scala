import com.treasure.analytics.Price

import scala.reflect.runtime.universe._

def classAccessors[T: TypeTag]: List[MethodSymbol] = typeOf[T].members.collect {
  case m: MethodSymbol if m.isCaseAccessor => m
}.toList

def foo: Unit = {
  println(extractFieldNames[Price].mkString(","))
}

def extractFieldNames[T <: Product](implicit m: Manifest[T]) =
  m.erasure.getDeclaredFields.map(_.getName)
