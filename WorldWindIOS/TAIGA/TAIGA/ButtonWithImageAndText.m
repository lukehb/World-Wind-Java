/*
 Copyright (C) 2013 United States Government as represented by the Administrator of the
 National Aeronautics and Space Administration. All Rights Reserved.
 
 @version $Id$
 */

#import "ButtonWithImageAndText.h"

@implementation ButtonWithImageAndText

- (ButtonWithImageAndText*) initWithImageName:(NSString*)imageName text:(NSString*)text size:(CGSize)size target:(id)
        target                         action:(SEL)action;
{
    self = [super initWithFrame:CGRectMake(0, 0, size.width, size.height)];

    [self setShowsTouchWhenHighlighted:YES];

    [self setTitle:text forState:UIControlStateNormal];
    [self.titleLabel setFont:[UIFont fontWithName:@"Helvetica-Bold" size:18]];
    [self.titleLabel setShadowOffset:CGSizeMake(0, -1)];
    CGFloat txtMargin = 0.5 * (self.frame.size.width - [text sizeWithFont:[[self titleLabel] font]].width);
    [self setTitleEdgeInsets:UIEdgeInsetsMake(0, -txtMargin, -30, 0)];

    UIImage* img = [UIImage imageNamed:imageName];
    CGFloat imgMargin = 0.5 * (self.frame.size.width - [img size].width);
    [self setImageEdgeInsets:UIEdgeInsetsMake(-20, imgMargin, 20, 0)];
    [self setImage:img forState:UIControlStateNormal];

    [self addTarget:target action:action forControlEvents:UIControlEventTouchUpInside];

    return self;
}

- (void) setFontSize:(int)fontSize
{
    [self.titleLabel setFont:[UIFont fontWithName:@"Helvetica-Bold" size:fontSize]];
}

- (void) setTextColor:(UIColor*)textColor
{
    [self setTitleColor:textColor forState:UIControlStateNormal];
}

- (void) highlight:(BOOL)highlight
{
    if (highlight)
        [self setBackgroundColor:[[UIColor alloc] initWithRed:1. green:1. blue:1. alpha:0.2]];
    else
        [self setBackgroundColor:[UIColor clearColor]];
}

@end