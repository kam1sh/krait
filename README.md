# krait
Configuration library for Kotlin, Java, and other JVM languages

## Why?
Configuration in JVM world is weird. Usually we use unexpressive, but build-in `-D` command line option or `.properties` file, sometimes there is also some environment variables,
rarely YAML, and more rarely you see wome mix of two of them. 

But people are different, and so are their projects, and sometimes you can encounter configuration if unexpected places, like databases or etcd/zookeeper. 

So... What if you could use all configuration sources from one place? Like, single interface.

I work with configuration a lot, because I'm a DevOps engineer, and I wanted to made configuration access powerful both in my personal projects, and in services of my colleagaues.

Meet Krait, configuration library, inspired by Dynaconf:
```kotlin
// Given properties:
// app.key = test
// app.numbers.first = 1
// app.numbers.second = 2
// app.loggers.0.name = app
// app.loggers.0.level = info
// app.loggers.1.name = framework
// app.loggers.1.level = warn
// Given variables:
// APP__NUMBERS__THIRD = 3
// APP__LOGGERS__2__NAME = orm
// APP__LOGGERS__2__LEVEL = debug
val kr = Krait {
  sources {
    add(EnvironmentSource("APP")) // use environment variables, starting from 'APP'
    add(PropertiesSource("app")) // will be accessed in second order
  }
}
// load dev profile. some sources support loading different profiles,
// but EnvironmentSource and PropertiesSource don't.
kr.load("dev")
assertEquals("test", kr["key"].text())
assertEquals(123, kr["numbers"]["first"].long())
assertEquals(3, kr["numbers"].entries(String::class.java).size) // yay, merge of lists of different sources!
for (node in kr["loggers"].list()) {println(node["level"].text())} // will be three items, so same merge will happen for lists
```
More examples in [tests](https://github.com/kam1sh/krait/tree/main/krait-core/src/test/kotlin/com/github/kam1sh/krait/core). Documentation will be available soon. Or later.

## Naming
This library named after [Krait Phantom](https://elite-dangerous.fandom.com/wiki/Krait_Phantom) from Elite: Dangerous.
