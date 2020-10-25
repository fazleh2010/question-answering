/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package grammar.read.questions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author elahi
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UriLabel {

    @JsonProperty("label")
    private String label;
    @JsonProperty("uri")
    private String uri;

    public String getLabel() {
        return label;
    }

    public String getUri() {
        return uri;
    }

}
