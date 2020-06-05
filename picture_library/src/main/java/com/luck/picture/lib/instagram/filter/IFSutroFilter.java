package com.luck.picture.lib.instagram.filter;

import android.content.Context;

import com.luck.picture.lib.R;


/**
 * Created by sam on 14-8-9.
 */
public class IFSutroFilter extends IFImageFilter {
    private static final String SHADER = "precision lowp float;\n" +
            " \n" +
            " varying highp vec2 textureCoordinate;\n" +
            " \n" +
            " uniform sampler2D inputImageTexture;\n" +
            " uniform sampler2D inputImageTexture2; //sutroMap;\n" +
            " uniform sampler2D inputImageTexture3; //sutroMetal;\n" +
            " uniform sampler2D inputImageTexture4; //softLight\n" +
            " uniform sampler2D inputImageTexture5; //sutroEdgeburn\n" +
            " uniform sampler2D inputImageTexture6; //sutroCurves\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "     \n" +
            "     vec3 texel = texture2D(inputImageTexture, textureCoordinate).rgb;\n" +
            "     \n" +
            "     vec2 tc = (2.0 * textureCoordinate) - 1.0;\n" +
            "     float d = dot(tc, tc);\n" +
            "     vec2 lookup = vec2(d, texel.r);\n" +
            "     texel.r = texture2D(inputImageTexture2, lookup).r;\n" +
            "     lookup.y = texel.g;\n" +
            "     texel.g = texture2D(inputImageTexture2, lookup).g;\n" +
            "     lookup.y = texel.b;\n" +
            "     texel.b\t= texture2D(inputImageTexture2, lookup).b;\n" +
            "     \n" +
            "     vec3 rgbPrime = vec3(0.1019, 0.0, 0.0); \n" +
            "     float m = dot(vec3(.3, .59, .11), texel.rgb) - 0.03058;\n" +
            "     texel = mix(texel, rgbPrime + m, 0.32);\n" +
            "     \n" +
            "     vec3 metal = texture2D(inputImageTexture3, textureCoordinate).rgb;\n" +
            "     texel.r = texture2D(inputImageTexture4, vec2(metal.r, texel.r)).r;\n" +
            "     texel.g = texture2D(inputImageTexture4, vec2(metal.g, texel.g)).g;\n" +
            "     texel.b = texture2D(inputImageTexture4, vec2(metal.b, texel.b)).b;\n" +
            "     \n" +
            "     texel = texel * texture2D(inputImageTexture5, textureCoordinate).rgb;\n" +
            "     \n" +
            "     texel.r = texture2D(inputImageTexture6, vec2(texel.r, .16666)).r;\n" +
            "     texel.g = texture2D(inputImageTexture6, vec2(texel.g, .5)).g;\n" +
            "     texel.b = texture2D(inputImageTexture6, vec2(texel.b, .83333)).b;\n" +
            "     \n" +
            "     \n" +
            "     gl_FragColor = vec4(texel, 1.0);\n" +
            " }\n";

    public IFSutroFilter(Context paramContext) {
        super(paramContext, SHADER);
        setRes();
    }

    private void setRes() {
        addInputTexture(R.drawable.vignette_map);
        addInputTexture(R.drawable.sutro_metal);
        addInputTexture(R.drawable.soft_light);
        addInputTexture(R.drawable.sutro_edge_burn);
        addInputTexture(R.drawable.sutro_curves);
    }
}
