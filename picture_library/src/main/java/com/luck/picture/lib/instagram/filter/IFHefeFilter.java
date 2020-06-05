package com.luck.picture.lib.instagram.filter;

import android.content.Context;

import com.luck.picture.lib.R;


/**
 * Created by sam on 14-8-9.
 */
public class IFHefeFilter extends IFImageFilter {
    private static final String SHADER = "precision lowp float;\n" +
            " \n" +
            " varying highp vec2 textureCoordinate;\n" +
            " \n" +
            " uniform sampler2D inputImageTexture;\n" +
            " uniform sampler2D inputImageTexture2;  //edgeBurn\n" +
            " uniform sampler2D inputImageTexture3;  //hefeMap\n" +
            " uniform sampler2D inputImageTexture4;  //hefeGradientMap\n" +
            " uniform sampler2D inputImageTexture5;  //hefeSoftLight\n" +
            " uniform sampler2D inputImageTexture6;  //hefeMetal\n" +
            " \n" +
            " void main()\n" +
            "{\t\n" +
            "\tvec3 texel = texture2D(inputImageTexture, textureCoordinate).rgb;\n" +
            "\tvec3 edge = texture2D(inputImageTexture2, textureCoordinate).rgb;\n" +
            "\ttexel = texel * edge;\n" +
            "\t\n" +
            "\ttexel = vec3(\n" +
            "                 texture2D(inputImageTexture3, vec2(texel.r, .16666)).r,\n" +
            "                 texture2D(inputImageTexture3, vec2(texel.g, .5)).g,\n" +
            "                 texture2D(inputImageTexture3, vec2(texel.b, .83333)).b);\n" +
            "\t\n" +
            "\tvec3 luma = vec3(.30, .59, .11);\n" +
            "\tvec3 gradSample = texture2D(inputImageTexture4, vec2(dot(luma, texel), .5)).rgb;\n" +
            "\tvec3 final = vec3(\n" +
            "                      texture2D(inputImageTexture5, vec2(gradSample.r, texel.r)).r,\n" +
            "                      texture2D(inputImageTexture5, vec2(gradSample.g, texel.g)).g,\n" +
            "                      texture2D(inputImageTexture5, vec2(gradSample.b, texel.b)).b\n" +
            "                      );\n" +
            "    \n" +
            "    vec3 metal = texture2D(inputImageTexture6, textureCoordinate).rgb;\n" +
            "    vec3 metaled = vec3(\n" +
            "                        texture2D(inputImageTexture5, vec2(metal.r, texel.r)).r,\n" +
            "                        texture2D(inputImageTexture5, vec2(metal.g, texel.g)).g,\n" +
            "                        texture2D(inputImageTexture5, vec2(metal.b, texel.b)).b\n" +
            "                        );\n" +
            "\t\n" +
            "\tgl_FragColor = vec4(metaled, 1.0);\n" +
            "}\n";

    public IFHefeFilter(Context paramContext) {
        super(paramContext, SHADER);
        setRes();
    }

    private void setRes() {
        addInputTexture(R.drawable.edge_burn);
        addInputTexture(R.drawable.hefe_map);
        addInputTexture(R.drawable.hefe_gradient_map);
        addInputTexture(R.drawable.hefe_soft_light);
        addInputTexture(R.drawable.hefe_metal);
    }
}
