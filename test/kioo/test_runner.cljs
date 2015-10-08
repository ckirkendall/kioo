(ns kioo.test-runner
  (:require [doo.runner :refer-macros [doo-all-tests]]
            [kioo.core-test]
            [kioo.reagent-test]
            [kioo.om-test]))

(doo-all-tests)
