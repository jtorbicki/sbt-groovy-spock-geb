package com.xyz.groovy

import geb.spock.GebSpec

class TestGeb extends GebSpec {
    def "go to local"() {
        when:
        go "http://localhost:8080"

        then:
        title == "title!"
    }
}
