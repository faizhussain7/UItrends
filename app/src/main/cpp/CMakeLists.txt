cmake_minimum_required(VERSION 3.22.1)

project("pretext_geometry")

set(NCNN_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/third_party/ncnn/${ANDROID_ABI}")
set(NCNN_LIB "${NCNN_ROOT}/lib/libncnn.a")
if(EXISTS "${NCNN_LIB}")
    set(PRETEXT_USE_NCNN ON)
    message(STATUS "Pretext: NCNN enabled for ${ANDROID_ABI}")
else()
    set(PRETEXT_USE_NCNN OFF)
    message(STATUS "Pretext: NCNN not found — object mode uses TFLite fallback")
endif()

set(PRETEXT_SOURCES
    pretext_native_lock.cpp
    pretext_geometry.cpp
    pretext_contour_math.cpp
    pretext_mask_contour.cpp
    pretext_geometry_jni.cpp
    pretext_yuv_rgb.cpp
    pretext_vision_orchestrator.cpp
    pretext_vision_jni.cpp
)

if(PRETEXT_USE_NCNN)
    list(APPEND PRETEXT_SOURCES pretext_ncnn_yolo.cpp)
endif()

add_library(pretext_geometry SHARED ${PRETEXT_SOURCES})

target_compile_features(pretext_geometry PRIVATE cxx_std_17)

target_compile_options(
    pretext_geometry
    PRIVATE
    -O3
    -ffast-math
    -fstack-protector-strong
    -D_FORTIFY_SOURCE=2
    -Wformat
    -Wformat-security
    -fvisibility=hidden
    -fno-exceptions
    -fno-rtti
)

target_link_options(
    pretext_geometry
    PRIVATE
    -Wl,-z,relro
    -Wl,-z,now
)

find_library(log-lib log)
find_library(android-lib android)

target_link_libraries(
    pretext_geometry
    ${log-lib}
    ${android-lib}
)

if(PRETEXT_USE_NCNN)
    target_compile_definitions(pretext_geometry PRIVATE PRETEXT_USE_NCNN=1)
    target_include_directories(pretext_geometry PRIVATE "${NCNN_ROOT}/include")
    target_compile_options(pretext_geometry PRIVATE -fopenmp)
    target_link_options(pretext_geometry PRIVATE -fopenmp -static-openmp)
    target_link_libraries(
        pretext_geometry
        "${NCNN_LIB}"
        jnigraphics
    )
endif()
