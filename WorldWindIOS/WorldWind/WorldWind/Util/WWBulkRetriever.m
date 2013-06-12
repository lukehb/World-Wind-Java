/*
 Copyright (C) 2013 United States Government as represented by the Administrator of the
 National Aeronautics and Space Administration. All Rights Reserved.

 @version $Id$
 */

#import "WorldWind/Util/WWBulkRetriever.h"
#import "WorldWind/Util/WWBulkRetrieverDataSource.h"
#import "WorldWind/Geometry/WWSector.h"
#import "WorldWind/WorldWind.h"

@implementation WWBulkRetriever

- (WWBulkRetriever*) initWithDataSource:(id <WWBulkRetrieverDataSource>)dataSource sector:(WWSector*)sector;
{
    if (dataSource == nil)
    {
        WWLOG_AND_THROW(NSInvalidArgumentException, @"Data source is nil")
    }

    if (sector == nil)
    {
        WWLOG_AND_THROW(NSInvalidArgumentException, @"Sector is nil")
    }

    self = [super init];

    _dataSource = dataSource;
    _sector = sector;
    _targetResolution = 0; // Indicates the best available resolution.

    return self;
}

- (WWBulkRetriever*) initWithDataSource:(id <WWBulkRetrieverDataSource>)dataSource sector:(WWSector*)sector
                       targetResolution:(double)resolution;
{
    if (dataSource == nil)
    {
        WWLOG_AND_THROW(NSInvalidArgumentException, @"Data source is nil")
    }

    if (sector == nil)
    {
        WWLOG_AND_THROW(NSInvalidArgumentException, @"Sector is nil")
    }

    self = [super init];

    _dataSource = dataSource;
    _sector = sector;
    _targetResolution = resolution;

    return self;
}

- (void) main
{
    @autoreleasepool
    {
        @try
        {
            if (![self mustStopBulkRetrieval])
            {
                [self performBulkRetrieval];
            }
        }
        @catch (NSException* exception)
        {
            WWLogE(@"during bulk retrieval", exception);
        }
        @finally
        {
            _dataSource = nil; // don't need the data source anymore
        }
    }
}

- (BOOL) mustStopBulkRetrieval
{
    return [self isCancelled] || [WorldWind isOfflineMode];
}

- (void) performBulkRetrieval
{
    [_dataSource performBulkRetrieval:self];
}

@end