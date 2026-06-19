cmake_minimum_required(VERSION 3.10.2)
project("sxtwl_bridge")

set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Enable the C wrapper so sxtwl_c static library is built
set(SXTWL_WRAPPER_C ON CACHE BOOL "Build the C wrapper" FORCE)

# Build sxtwl_cpp as static library
add_subdirectory(sxtwl_cpp ${CMAKE_CURRENT_BINARY_DIR}/sxtwl_cpp)

# Build JNI bridge shared library
add_library(sxtwl_bridge SHARED sxtwl_bridge.c)
target_link_libraries(sxtwl_bridge sxtwl_c sxtwl)