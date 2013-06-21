/*
 Copyright (C) 2013 United States Government as represented by the Administrator of the
 National Aeronautics and Space Administration. All Rights Reserved.

 @version $Id$
 */

#import "WorldWind/Shaders/WWBasicProgram.h"
#import "WorldWind/WWLog.h"

#define STRINGIFY(A) #A
#import "WorldWind/Shaders/BasicShader.vert"
#import "WorldWind/Shaders/BasicShader.frag"

@implementation WWBasicProgram

- (WWBasicProgram*) init
{
    self = [super initWithShaderSource:BasicVertexShader fragmentShader:BasicFragmentShader];

    _vertexPointLocation = (GLuint) [self getAttributeLocation:@"vertexPoint"];
    mvpMatrixLocation = (GLuint) [self getUniformLocation:@"mvpMatrix"];
    colorLocation = (GLuint) [self getUniformLocation:@"color"];

    [self bind];
    glEnableVertexAttribArray(_vertexPointLocation);
    glUseProgram(0);

    return self;
}

- (void) loadModelviewProjection:(WWMatrix* __unsafe_unretained)matrix
{
    if (matrix == nil)
    {
        WWLOG_AND_THROW(NSInvalidArgumentException, @"Matrix is nil")
    }

    [WWGpuProgram loadUniformMatrix:matrix location:mvpMatrixLocation];
}

- (void) loadColor:(WWColor* __unsafe_unretained)color
{
    if (color == nil)
    {
        WWLOG_AND_THROW(NSInvalidArgumentException, @"Color is nil")
    }

    [WWGpuProgram loadUniformColor:color location:colorLocation];
}

- (void) loadPickColor:(unsigned int)color
{
    [WWGpuProgram loadUniformPickColor:color location:colorLocation];
}

@end