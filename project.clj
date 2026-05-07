(defproject witch-house/pronouns "1.12.0-SNAPSHOT"
  :description "Pronoun.is is a website for personal pronoun usage examples"
  :url "https://pronoun.is"
  :license "GNU Affero General Public License 3.0"
  :dependencies [[compojure "1.7.2"]
                 [environ "1.2.0"]
                 [hiccup "2.0.0"]
                 [org.clojure/clojure "1.12.4"]
                 [org.clojure/tools.logging "1.3.1"]
                 [org.slf4j/slf4j-simple "2.0.17"]
                 [ring-logger "1.1.1"]
                 [ring/ring-devel "1.15.4"]
                 [ring/ring-jetty-adapter "1.15.4"]]
  :min-lein-version "2.4.0"
  :plugins [[lein-environ "1.2.0"]
            [lein-ring "0.12.6"]
            [lein-ancient "0.7.0"]
            [jonase/eastwood "1.4.3"]]
  :uberjar-name "pronouns-standalone.jar"
  :main pronouns.web
  :profiles {:production {:env {:production true}}
             :dev {:ring {:handler pronouns.web/dev-app}}
             :uberjar {:aot :all
                       :ring {:handler pronouns.web/prod-app}
                       :dependencies [[com.github.clj-easy/graal-build-time "1.0.5"]]}}
  :jvm-opts ["-Dclojure.tools.logging.factory=clojure.tools.logging.impl/slf4j-factory"]
  :ring {:handler pronouns.web/app})
